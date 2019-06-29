package com.txy.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class NIOClient {
    private final InetSocketAddress serverAddress = new InetSocketAddress("localhost",8080);
    private Selector selector = null;
    private SocketChannel client = null;

    private String nickName = "";
    private Charset charset = Charset.forName("UTF-8");
    private static String USER_EXIST = "系统提示：该昵称已经存在，请换一个昵称";
    private static String USER_CONTENT_SPILIT = "#@#";

    public NIOClient() throws IOException {
        client = SocketChannel.open(serverAddress);
        client.configureBlocking(false);
        selector = Selector.open();
        client.register(selector, SelectionKey.OP_READ);
    }

    public void session(){
        new Reader().start();
        new Writer().start();
    }

    private class Writer extends Thread {
        @Override
        public void run() {
            Scanner scan = new Scanner(System.in);
            try {
                while (scan.hasNextLine()){
                    String line = scan.nextLine();
                    if("".equals(line)) continue;
                    if("".equals(nickName)){
                        nickName = line;
                        line = nickName+USER_CONTENT_SPILIT;
                    } else {
                        line = nickName+USER_CONTENT_SPILIT+line;
                    }
                    client.write(charset.encode(line));
                }
                scan.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Reader extends Thread {
        @Override
        public void run() {
            try {
                while (true){
                    int readyChannels = selector.select();
                    if(readyChannels == 0) continue;
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                    while (keyIterator.hasNext()){
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        process(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void process(SelectionKey key) throws IOException {
        if(key.isReadable()){
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer buff = ByteBuffer.allocate(1024);
            String content = "";
            while (sc.read(buff)>0){
                buff.flip();
                content += charset.decode(buff);
            }
            if(USER_EXIST.equals(content)){
                nickName = "";
            }
            System.out.println(content);
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public static void main(String[] args) throws IOException {
        new NIOClient().session();
    }

}
