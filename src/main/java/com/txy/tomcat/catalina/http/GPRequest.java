package com.txy.tomcat.catalina.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

public class GPRequest {
    private ChannelHandlerContext ctx;
    private HttpRequest r;

    public GPRequest(ChannelHandlerContext ctx, HttpRequest r) {
        this.ctx = ctx;
        this.r = r;
    }

    public String getUri() {
        return r.getUri();
    }

    public String getMethod(){
        return r.getMethod().name();
    }

    public Map<String,List<String>> getParameters(){
        QueryStringDecoder decoder = new QueryStringDecoder(r.getUri());
        return decoder.parameters();
    }

    public String getParameters(String name){
        Map<String, List<String>> parameters = getParameters();
        List<String> param = parameters.get(name);
        if(param == null || param.size() == 0){
            return null;
        } else {
            return param.get(0);
        }
    }

}
