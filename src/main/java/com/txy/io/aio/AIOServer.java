package com.txy.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AIOServer {

    int port;

    public AIOServer(int port) {
        this.port = port;
    }

    private void listen() {
        try {
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(this.port));

            server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel client, Object attachment) {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        Future<Integer> read = client.read(buffer);
                        Integer len = read.get();
                        buffer.flip();
                        System.out.println(new String(buffer.array(),0,len));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void failed(Throwable exc, Object attachment) {

                }
            });
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        new AIOServer(8080).listen();

    }



}
