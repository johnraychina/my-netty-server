package com.zhangyi.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 将 handler 安装到 pipeline 中从而实现各种功能
 *
 * @author Zhang Yi
 */
public class ChatServerInitializer extends ChannelInitializer<Channel> {

    private final ChannelGroup channelGroup;

    public ChatServerInitializer(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // decode(request bytes)
        // to: HttpRequest, HttpContent, LastHttpContent
        // encode(HttpResponse, HttpContent, LastHttpContent)
        // to: response bytes
        pipeline.addLast(new HttpServerCodec());

        //大块数据写处理
        pipeline.addLast(new ChunkedWriteHandler());

        //http聚合消息
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));

        //（自定义）http内容处理，这里也处理升级websocket协议后的handler替换
        pipeline.addLast(new HttpRequestHandler("/ws"));

        //websocket握手协议，websocket frame处理
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //（自定义）websocket文本帧处理
        pipeline.addLast(new TextWebSocketFrameHandler(channelGroup));

    }
}
