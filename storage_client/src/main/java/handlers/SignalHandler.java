package handlers;

import callbacks.Callback;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import models.TransmittedSignal;
import store.ApplicationStore;

public class SignalHandler extends SimpleChannelInboundHandler<TransmittedSignal> {

    public SignalHandler() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TransmittedSignal signal) throws Exception {
        if (ApplicationStore.callback != null) {
            ApplicationStore.callback.processAnswer(signal);
        }
    }
}
