package handlers;

import callbacks.Callback;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import models.Message;

public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    private Callback callback;

    public MessageHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        callback.processAnswer(message);
    }
}
