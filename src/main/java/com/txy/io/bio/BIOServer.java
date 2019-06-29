package com.txy.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {

    ServerSocket serverSocket;

    public BIOServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listener() throws IOException {
        while (true) {
            Socket client = serverSocket.accept();//这是阻塞的，没有客户端是阻塞的
            InputStream is = client.getInputStream();
            byte[] buff = new byte[1024];
            int len = is.read(buff);
            if(len!=-1){
                String msg = new String(buff, 0, len);
                System.out.println(msg);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new BIOServer(8080).listener();
    }

}
