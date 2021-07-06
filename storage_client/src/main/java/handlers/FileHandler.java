package handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.FileResponse;
import store.ApplicationStore;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<FileResponse> {

    public FileHandler() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileResponse signal) throws Exception {
        if (ApplicationStore.fileCallback != null) {
            ApplicationStore.fileCallback.processAnswer(signal);
        }
    }
}
