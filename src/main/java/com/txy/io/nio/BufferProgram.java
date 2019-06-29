package com.txy.io.nio;

import com.txy.util.IOUtils;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class BufferProgram {

    @Test
    public void test() throws IOException {
        FileInputStream is = new FileInputStream("C:\\Users\\TianXiang\\Desktop\\file.txt");
        FileChannel fc = is.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        fc.read(buffer);
        buffer.flip();
        while (buffer.remaining()>0){
            byte b = buffer.get();
            System.out.print((char)b);
        }
    }

    //IO映射
    //对内存的更改，直接映射到硬盘上
    @Test
    public void testMapperByteBuffer() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("C:\\Users\\TianXiang\\Desktop\\file.txt", "rw");
        FileChannel fileChannel = raf.getChannel();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
        mappedByteBuffer.put(0,(byte)97);
        mappedByteBuffer.put(10,(byte)122);
        IOUtils.close(raf);
    }

}
