package com.zhangyi.netty.inaction;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author 张义 reed.zy@alibaba-inc.com
 */
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage:" + EchoServer.class.getSimpleName() + " <port>");
            return;
        }

        EchoServer echoServer = new EchoServer(Integer.parseInt(args[0]));
        echoServer.start();
    }

    private void start() throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();
        final ChannelHandler serverHandler = new EchoServerHandler();
        try {

            // 配置启动引导程序
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(serverHandler);
                    }
                });

            ChannelFuture channelFuture = bootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
