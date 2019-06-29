package com.txy.chat.server.handler;

import com.txy.chat.process.IMProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private IMProcessor processor = new IMProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        processor.process(ctx.channel(),msg.text());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        processor.logout(ctx.channel());
    }

    //针对自定义的协议
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

    }
}
