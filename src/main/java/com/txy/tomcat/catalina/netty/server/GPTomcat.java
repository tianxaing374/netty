package com.txy.tomcat.catalina.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;

public class GPTomcat {

    public void start(int port) throws Exception {
        //boss线程
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
                    //子线程处理，Handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel client) throws Exception {
                            //业务逻辑链路，编码器
                            client.pipeline().addLast(new HttpRequestEncoder());
                            //解码器
                            client.pipeline().addLast(new HttpRequestDecoder());
                            //业务逻辑处理
                            client.pipeline().addLast(new GPTomcatHandler());
                        }
                    })
                    //主线程的配置,128分配线程的最多
                    .option(ChannelOption.SO_BACKLOG,128)
                    //子线程的配置
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            //阻塞
            ChannelFuture future = server.bind(port).sync();
            System.out.println("GPTomcat已经启动："+port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        try {
            new GPTomcat().start(8080);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
