package com.txy.rpc.registry;

import com.txy.rpc.msg.InvokerMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {

    public static ConcurrentHashMap<String,Object> registryMap = new ConcurrentHashMap<>();
    private List<String> classCache = new ArrayList<>();

    public RegistryHandler() {
        scanClass("com.txy.rpc.provider");
        doRegistry();
    }

    //约定写在com.txy.rpc.provider下的类，都是可以对外提供服务的实现类
    //IOC容器简约
    private void scanClass(String packageName){
        URL url = getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if(file.isDirectory()){
                scanClass(packageName+"."+file.getName());
            } else {
                classCache.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }
    }

    //把扫描的class实例化，放到map中，服务名叫接口名
    private void doRegistry(){
        for (String className : classCache) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?>[] interfaces = clazz.getInterfaces();
                if(interfaces.length>0){
                    Class<?> anInterface = interfaces[0];
                    registryMap.put(anInterface.getName(),clazz.newInstance());
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        if (msg instanceof InvokerMsg) {
            InvokerMsg request = (InvokerMsg) msg;
            if(registryMap.containsKey(request.getClassName())){
                Object clazz = registryMap.get(request.getClassName());
                Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParams());
                result = method.invoke(clazz,request.getValues());
            }
            ctx.writeAndFlush(result);
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
//    }
}
