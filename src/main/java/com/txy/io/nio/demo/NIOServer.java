package com.txy.io.nio.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    private int port = 8080;
    private InetSocketAddress address = null;
    private Selector selector;

    public NIOServer(int port) {
        try {
            this.port = port;
            this.address = new InetSocketAddress(this.port);
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(this.address);
            server.configureBlocking(false);
            //得到一个大管家
            selector = Selector.open();
            //为sever分配一个管家，并等待一个连接
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器准备OK,监听端口是："+this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        //开始轮训
        //每一次轮训只能干一件事，这是同步非阻塞
        try {
            while (true) {
                //大厅排队人数
                int wait = this.selector.select();
                if(wait == 0) continue;
                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    process(key);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //一个SelectionKey只能干一件事
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector,SelectionKey.OP_READ);
        } else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            int len = client.read(buffer);
            if(len>0){
                buffer.flip();
                String content = new String(buffer.array(), 0, len);
                System.out.println(content);
//                client.register(selector,SelectionKey.OP_WRITE);//下一路开始写
            }
            buffer.clear();
        } else if(key.isWritable()){
            SocketChannel client = (SocketChannel) key.channel();
            client.write(ByteBuffer.wrap("HelloWorld".getBytes()));
            client.close();
//            client.register(selector,SelectionKey.OP_READ);
        }
    }

    public static void main(String[] args) {
        new NIOServer(8080).listen();
    }

}
