package com.zhangyi.nio.test;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Zhang Yi
 */
public class NIOServer {
    private static final int PORT = 54321;

    public static void main(String[] args) throws Exception {

        // 开启一个channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 绑定端口
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        // 打开一个多路复用器
        Selector selector = Selector.open();
        // 绑定多路复用器和channel
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 获取到达的事件
        // todo select方式是：网卡收到网络信号，操作系统内核有数据了，但是不会通知用户线程，用户线程自己轮询判断socket状态，然后进行读写
        while (selector.select() > 0) {
            Set<SelectionKey> keys = selector.keys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    // 处理逻辑
                    accept(selectionKey);
                }
                if (selectionKey.isReadable()) {
                    // 处理逻辑
                    read(selectionKey);
                }
            }
        }
    }

    public static void read(SelectionKey selectionKey) {
        selectionKey.attachment();
    }

    public static void accept(SelectionKey selectionKey) {

    }
}
