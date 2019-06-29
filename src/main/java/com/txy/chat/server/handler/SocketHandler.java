package com.txy.chat.server.handler;

import com.txy.chat.process.IMProcessor;
import com.txy.chat.protocol.IMMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SocketHandler extends SimpleChannelInboundHandler<IMMessage> {

    private IMProcessor processor = new IMProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
        processor.process(ctx.channel(),msg);
    }
}
