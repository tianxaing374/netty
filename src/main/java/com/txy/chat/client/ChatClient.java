package com.txy.chat.client;

import com.txy.chat.client.handler.ChatClientHandler;
import com.txy.chat.protocol.IMDecoder;
import com.txy.chat.protocol.IMEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ChatClient {

    private ChatClientHandler clientHandler;
    private String host;
    private int port;

    public ChatClient(String nickName){
        this.clientHandler = new ChatClientHandler(nickName);
    }

    public void connect(String host,int port){
        this.port = port;
        this.host = host;
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    //当配置完之后，pipeline中就没有ChannelInitializer了
                    //ChannelInitializer只用于配置
                    .handler(new ChannelInitializer<SocketChannel>() {
                        //对应为Bootstrap.java里面的p.addLast(config.handler());
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IMDecoder());
                            pipeline.addLast(new IMEncoder());
                            pipeline.addLast(clientHandler);
                        }
                    });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatClient("Tianxiang").connect("127.0.0.1",80);
    }

}
