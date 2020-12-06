package com.zhangyi.nio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * @author Zhang Yi
 */

public class ReactorDemo {
    private Selector selector;
    private Selector ioSelector;


    public ReactorDemo() throws IOException {
        initServer();
    }

    private void initServer() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(9999));

        selector = Selector.open();
        ioSelector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server 启动");
    }

    public void startServer() {
        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    majorListen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    subListen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void majorListen() throws IOException {
        System.out.println("主selector启动");
        while (selector.select() > 0) {
            System.out.println("主selector有事件");
            Set<SelectionKey> set = selector.selectedKeys();
            Iterator<SelectionKey> iterator = set.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    new Acceptor().process(selectionKey);
                }
                iterator.remove();
            }

        }
    }

    public void subListen() throws IOException {
        System.out.println("子selector启动");
        while (true) {
            if (ioSelector.select(100) <= 0) {
                continue;
            }
            System.out.println("子selector有事件");
            Set<SelectionKey> set = ioSelector.selectedKeys();
            Iterator<SelectionKey> iterator = set.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                selectionKey.attach(new ProcessHandler());
                dispatch(selectionKey, true);
                iterator.remove();
            }

        }
    }

    public void dispatch(SelectionKey selectionKey, boolean isSub) {
        Handler handler = (Handler) selectionKey.attachment();
        if (handler != null) {
            handler.process(selectionKey);
        }
    }

    private interface Handler {
        void process(SelectionKey selectionKey);
    }

    private class Acceptor implements Handler {

        @Override
        public void process(SelectionKey selectionKey) {
            try {
                ServerSocketChannel serverSocketChannel =
                    (ServerSocketChannel) selectionKey.channel();
                SocketChannel channel = serverSocketChannel.accept();
                System.out.println("获取一个链接：" + channel.getRemoteAddress());
                channel.configureBlocking(false);
                channel.register(ioSelector,
                    SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                ioSelector.wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ProcessHandler implements Handler {

        @Override
        public void process(SelectionKey selectionKey) {
            try {
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.clear();
                StringBuilder sb = new StringBuilder();
                int read = 0;
                if ((read = channel.read(buffer)) > 0) {
                    buffer.flip();
                    sb.append(Charset.forName("UTF-8").newDecoder().decode(buffer));
                    buffer.clear();
                } else if (read == 0) {
                    return;
                } else if (read == -1) {
                    if (selectionKey.isValid()) {
                        selectionKey.cancel();
                        channel.close();
                    }
                }
                System.out.printf("收到 %s 发来的：%s\n",
                    channel.getRemoteAddress(), sb);
                buffer.clear();
                buffer.put(("收到，你发来的是：" + sb + "\r\n").getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                channel.write(buffer);
                System.out.printf("回复 %s 内容是： %s\n",
                    channel.getRemoteAddress(), sb);
                channel.register(ioSelector, SelectionKey.OP_READ, buffer.clear());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ReactorDemo().startServer();
    }

}
