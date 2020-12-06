package com.zhangyi.netty;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @author 张义 reed.zy@alibaba-inc.com
 */
public class NIOClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(1234);
        socketChannel.connect(address);
        RandomAccessFile file = new RandomAccessFile(
            NIOClient.class.getClassLoader().getResource("test.txt").getFile(), "rw");
        FileChannel channel = file.getChannel();
        channel.transferTo(0, channel.size(), socketChannel);
        channel.close();
        file.close();
        socketChannel.close();
    }
}
