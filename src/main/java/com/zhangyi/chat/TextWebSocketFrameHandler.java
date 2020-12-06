package com.zhangyi.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;

/**
 * websocket 文本格式帧处理
 *
 * @author Zhang Yi
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup channelGroup;

    public TextWebSocketFrameHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 通知所有已经连接 websocket 客户端：新的客户端已经连接上了
        if (evt == WebSocketServerProtocolHandler
            .ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            channelGroup.writeAndFlush(new TextWebSocketFrame("用户[" + ctx.channel() + "] 已经加入聊天室"));
            channelGroup.add(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //增加消息的引用计数器，并将它写到已经<em>所有</em>连接的客户端，从而实现聊天室的每个人都能看到消息
        // 为什么一定要retain呢？
        // 由于channelRead0返回时会将msg引用计数器减少
        // 所有操作都是异步的 channelRead0可能在writeAndFlush完成前 返回，计数器减一
        // 如果不retain，可能会访问一个失效的引用
        channelGroup.writeAndFlush(msg.retain());
    }
}
