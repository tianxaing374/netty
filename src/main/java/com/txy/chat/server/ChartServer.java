package com.txy.chat.server;

import com.txy.chat.protocol.IMDecoder;
import com.txy.chat.protocol.IMEncoder;
import com.txy.chat.server.handler.HttpHandler;
import com.txy.chat.server.handler.SocketHandler;
import com.txy.chat.server.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ChartServer {

    public void start(int port) throws Exception {
        //boss线程
        //如果传参为1，就是单线程new NioEventLoopGroup(1)
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //Work线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //netty服务
            ServerBootstrap server = new ServerBootstrap();
            //主从线程模型
            server.group(bossGroup,workerGroup)
                    //主线程处理类
                    .channel(NioServerSocketChannel.class)
                    //可以加权限控制
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                        }
                    })
                    //子线程处理，Handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel client) throws Exception {
                            ChannelPipeline pipeline = client.pipeline();

                            //支持自定义socket协议
                            pipeline.addLast(new IMDecoder());
                            pipeline.addLast(new IMEncoder());
                            pipeline.addLast(new SocketHandler());

                            //解码和编码HTTP请求
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            //用于处理文件流
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpHandler());

                            //支持websocket协议
                            pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                            pipeline.addLast(new WebSocketHandler());

                        }
                    })
                    //主线程的配置,128分配线程的最多
                    .option(ChannelOption.SO_BACKLOG,128)
                    //子线程的配置
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            //阻塞，等待客户端连接
            ChannelFuture future = server.bind(port).sync();
            System.out.println("Server已经启动："+port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        try {
            new ChartServer().start(80);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
