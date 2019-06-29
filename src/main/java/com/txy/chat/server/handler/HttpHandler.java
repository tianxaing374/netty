package com.txy.chat.server.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

@Log4j
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private URL baseURL = HttpHandler.class.getProtectionDomain().getCodeSource().getLocation();
    private final String WEB_ROOT = "webroot";

    private File getFileFromRoot(String fileName) throws URISyntaxException {
        String path = baseURL.toURI().getPath() + WEB_ROOT + "/" + fileName;
        path = path.replace("//","/");
        return new File(path);
    }

    //netty中方法中加了0，都是实现类的方法，不是接口
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.getUri();
        String page = "/".equals(uri) ? "chat.html" : uri;
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(getFileFromRoot(page), "r");
        } catch (Exception e) {
            //log.info("file not found:"+getFileFromRoot(page).getName());
            //如果http不响应。则进入websocket
            ctx.fireChannelRead(request.retain());
            return;
        }
        String contextType = "text/html;";

        if(uri.endsWith(".css")){
            contextType = "text/css;";
        } else if(uri.endsWith(".js")){
            contextType = "text/javascript;";
        } else if(uri.toLowerCase().matches("(jpg|png|gif|ico)$")){
            String ext = uri.substring(uri.lastIndexOf(".")+1);
            contextType = "image/"+ext+";";
        } else if(uri.endsWith(".ico")){
            contextType = "image/x-icon";
        }

        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,contextType+"charset=utf-8;");

        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        if(keepAlive){
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,file.length());
            response.headers().set(HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response);
        ctx.write(new DefaultFileRegion(file.getChannel(),0,file.length()));
        //清空缓冲区
        ChannelFuture f = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        //不是长连接
        if(!keepAlive){
            f.addListener(ChannelFutureListener.CLOSE);
        }
        file.close();
    }
}
