package handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.FileResponse;
import store.ApplicationStore;

import java.util.UUID;

@Slf4j
public class FileResponseHandler extends SimpleChannelInboundHandler<FileResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileResponse fileResponse) throws Exception {
        UUID uuid = fileResponse.getUuid();
        if (fileResponse.isStatus()) {
            if (ApplicationStore.filesRequests.containsKey(uuid)) {
                ApplicationStore.filesRequests.get(uuid).accept(fileResponse);
            }
        }
        else {
            ApplicationStore.filesRequests.remove(uuid);
        }
    }
}
