package com.txy.io.nio.buffer;

import org.junit.Test;

import java.nio.IntBuffer;

public class BufferTestDemo {

    @Test
    public void testIntBuffer() {
        IntBuffer buffer = IntBuffer.allocate(8);
        for (int i = 0; i < buffer.capacity()/2; i++) {
            int j = 2*(i+1);
            buffer.put(j);
        }
        //重设此缓冲区，将限制设置为当前位置，然后将当前位置设置为0
        //对Buffer中的值进行操作
        //固定缓冲区的某些值，告诉缓冲区，我要操作
        // 如果再往缓冲区中写数据，不要再覆盖我固定状态之前的数据了
        //将limit置为当前position的值，在吧position设置为0
        buffer.flip();
        // buffer.put(0);
        // buffer.flip();
        while (buffer.hasRemaining()){
            int i = buffer.get();
            System.out.print(i+" ");
        }
    }

}
