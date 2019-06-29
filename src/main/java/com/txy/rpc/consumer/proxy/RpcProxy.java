package com.txy.rpc.consumer.proxy;

import com.txy.rpc.msg.InvokerMsg;
import com.txy.rpc.registry.RegistryHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxy {

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<?> clazz){
        MethodProxy method = new MethodProxy(clazz);
        T result = (T) Proxy.newProxyInstance(RpcProxy.class.getClassLoader(), new Class[]{clazz}, method);
        return result;
    }

}

class MethodProxy implements InvocationHandler{

    private Class<?> clazz;

    public MethodProxy(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //如果传进来的是一个已经实现的类，直接忽略
        if(Object.class.equals(method.getDeclaringClass())){
            return method.invoke(this,args);
        } else {
            return rpcInvoker(method,args);
        }
    }

    public Object rpcInvoker(Method method,Object[] args){
        InvokerMsg msg = new InvokerMsg();
        msg.setClassName(this.clazz.getName());
        msg.setMethodName(method.getName());
        msg.setParams(method.getParameterTypes());
        msg.setValues(args);
        NioEventLoopGroup group = new NioEventLoopGroup();
        final RpcProxyHandler handler = new RpcProxyHandler();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //处理拆包、粘包的解码编码器
                            pipeline.addLast("frameDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            pipeline.addLast("frameEncoder",new LengthFieldPrepender(4));
                            //处理序列化的解码编码器，jdk默认的
                            pipeline.addLast("encoder",new ObjectEncoder());
                            pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            //业务逻辑
                            pipeline.addLast(handler);
                        }
                    });
            ChannelFuture f = b.connect("localhost", 8080).sync();
            f.channel().writeAndFlush(msg).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
        return handler.getResult();
    }

}
