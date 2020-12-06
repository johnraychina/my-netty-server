package com.zhangyi.chat;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

/**
 * 《Netty 实战》
 * https://github.com/normanmaurer/netty-in-action/tree/2.0-SNAPSHOT/chapter12
 *
 * @author Zhang Yi
 */
public class ChatServer {

    // event loop
    private EventLoopGroup group = new NioEventLoopGroup();

    //创建 DefaultChannelGroup 保存已连接的 WebSocket Channel
    private ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    //建立的channel连接
    private Channel channel;

    public ChannelFuture start(InetSocketAddress address) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
            .channel(NioServerSocketChannel.class)
            .childHandler(createInitializer(channelGroup));

        ChannelFuture future = bootstrap.bind(address);
        future.syncUninterruptibly();

        channel = future.channel();

        return future;
    }

    public ChannelHandler createInitializer(ChannelGroup channelGroup) {
        return new ChatServerInitializer(channelGroup);
    }

    /**
     * 关掉channel, channelGroup, event loop group
     */
    public void destroy() {
        if (channel != null) {
            channel.close();
        }

        channelGroup.close();

        group.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        int port = 9000;
        final ChatServer server = new ChatServer();
        ChannelFuture channelFuture =  server.start(new InetSocketAddress(port));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.destroy();
            }
        });

        channelFuture.channel().closeFuture().syncUninterruptibly();
    }

}
