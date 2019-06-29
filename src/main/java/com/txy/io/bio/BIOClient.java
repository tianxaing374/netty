package com.txy.io.bio;

import com.txy.util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class BIOClient {

    public static void main(String[] args) throws IOException {
        Socket client = new Socket("localhost", 8080);
        OutputStream os = client.getOutputStream();
        String name = UUID.randomUUID().toString();
        os.write(name.getBytes());
//        InputStream inputStream = client.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//        System.out.println(reader.readLine());
//        IOUtils.close(reader);
//        IOUtils.close(inputStream);
        IOUtils.close(os);
        IOUtils.close(client);
    }

}
