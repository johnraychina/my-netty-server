package com.zhangyi.chat;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * @author Zhang Yi
 */
public class SecureChatServer extends ChatServer {

    private SslContext sslContext;

    public SecureChatServer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public ChannelHandler createInitializer(ChannelGroup channelGroup) {
        return new SecureChatServerInitializer(channelGroup, sslContext);
    }

    public static void main(String[] args) throws CertificateException, SSLException {
        int port = 9000;


        // 最简单证书：自签名证书，不需要CA机构认证
        SelfSignedCertificate cert = new SelfSignedCertificate();
        // 利用证书构造server ssl context
        SslContext sslContext = SslContextBuilder.forServer(cert.certificate(), cert.privateKey()).build();

        SecureChatServer server = new SecureChatServer(sslContext);
        ChannelFuture future = server.start(new InetSocketAddress(port));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.destroy();
            }
        });

        future.channel().closeFuture().syncUninterruptibly();

    }
}
