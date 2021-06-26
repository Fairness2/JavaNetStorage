package handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.FilePart;
import models.FileResponse;
import store.ApplicationStore;

@Slf4j
public class FilePartHandler extends SimpleChannelInboundHandler<FilePart> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FilePart signal) throws Exception {
        if (ApplicationStore.fileCallback != null) {
            ApplicationStore.fileCallback.processAnswer(signal);
        }
    }
}
