package com.txy.io.nio;

import com.txy.util.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    private int port = 8080;
    private Charset charset = Charset.forName("UTF-8");
    private static HashSet<String> users = new HashSet<>();
    private static String USER_EXIST = "系统提示：用户已存在";
    private static String USER_CONTENT_SPILIT = "#@#";
    private Selector selector = null;

    public NIOServer(int port) throws IOException {
        this.port = port;
        //要想富，先修路
        ServerSocketChannel server = ServerSocketChannel.open();
        //绑定地址和端口
        server.bind(new InetSocketAddress(this.port));
        server.configureBlocking(false);
        //开启Selector
        //Selector相当于排队叫号大厅
        //可以在接待大厅阻塞，但是不可以在大路channel上阻塞
        selector = Selector.open();
        //将排队叫号大厅绑定到路口，并且可以开始接待
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务已经启动，监听端口是："+this.port);
    }

    public void listener() throws IOException {
        //这里不会阻塞
        //CPU工作频率可控了，是可控的固定值
        while (true){
            //在轮训，在服务大厅中，到底有多少人排毒
            int wait = selector.select();
            if(wait == 0) continue;
            //取号，默认分配一个号码，Set保持唯一
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                //处理完成号码，打发走人
                iterator.remove();
                //处理逻辑
                process(key);
            }
        }
    }

    private void process(SelectionKey key) throws IOException {
        //判断客户端确定已经进入了服务大厅，并且已经连接好了，可以实现交互
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            //非阻塞模式
            client.configureBlocking(false);
            //注册选择器，并设置为读取模式，收到一个请求，然后起一个SocketChannel，并注册到Selector上
            client.register(selector,SelectionKey.OP_READ);
            //将此对应的Channel设置为准备接受其他客户端的请求
            key.interestOps(SelectionKey.OP_ACCEPT);
            client.write(charset.encode("请输入昵称"));
        }
        //处理来之客户端的数据读取请求
        if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buff = ByteBuffer.allocate(1024);
            StringBuffer content = new StringBuffer();
            try {
                while (client.read(buff)>0){
                    buff.flip();
                    content.append(charset.decode(buff));
                }
                //将此对应的channel设置为准备下一次接受数据
                key.interestOps(SelectionKey.OP_READ);
            } catch (IOException e) {
                key.cancel();
                IOUtils.close(key.channel());
            }
            if(content.length()>0){
                String[] arrayContent = content.toString().split(USER_CONTENT_SPILIT);
                //注册用户
                if(arrayContent!=null && arrayContent.length == 1){
                    String nickName = arrayContent[0];
                    if(users.contains(nickName)){
                        client.write(charset.encode(USER_EXIST));
                    } else {
                        users.add(nickName);
                        int onlineCount = onlineCount();
                        String message = "欢迎" + nickName + " 进入聊天室！当前在线人数：" + onlineCount;
                        broadCast(null,message);
                    }
                } else if(arrayContent!=null && arrayContent.length>1){
                    String nickName = arrayContent[0];
                    String message = content.substring(nickName.length() + USER_CONTENT_SPILIT.length());
                    message = nickName + " 说 " + message;
                    if(users.contains(nickName)){
                        broadCast(client,message);
                    }
                }
            }
        }

    }

    private void broadCast(SocketChannel client, String message) throws IOException {
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();
            if(targetChannel instanceof SocketChannel && targetChannel!=client){
                SocketChannel target = (SocketChannel) targetChannel;
                target.write(charset.encode(message));
            }
        }
    }

    private int onlineCount() {
        int res = 0;
        for (SelectionKey key : selector.keys()) {
            Channel target = key.channel();
            if(target instanceof SocketChannel){
                res++;
            }
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        new NIOServer(8080).listener();
    }

}
