package com.txy.io.bio;

import com.txy.util.IOUtils;
import org.junit.Test;

import java.io.*;

public class ReadFileDemo {

    @Test
    public void test() throws IOException {
        InputStream is = new FileInputStream("C:\\Users\\TianXiang\\Desktop\\file.txt");
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        System.out.println(bf.readLine());
        System.out.println(bf.readLine());
        System.out.println(bf.readLine());
        System.out.println(bf.readLine());
        IOUtils.close(is);
        IOUtils.close(bf);
    }

}
