package com.zhangyi.chat;

import javax.net.ssl.SSLEngine;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

/**
 * @author Zhang Yi
 */
public class SecureChatServerInitializer extends ChatServerInitializer {

    // SSL 上下文（证书、秘钥配置）
    private final SslContext sslContext;

    public SecureChatServerInitializer(ChannelGroup channelGroup, SslContext sslContext) {
        super(channelGroup);
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        super.initChannel(ch);

        // ssl引擎，加密压缩等需要缓冲空间
        SSLEngine engine = sslContext.newEngine(ch.alloc());
        engine.setUseClientMode(false);

        // 要加上对应handler即可实现ssl通信
        ch.pipeline().addFirst(new SslHandler(engine));

    }
}
