package handlers;

import file_tools.FileDirector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.FilePart;
import models.FileRequest;
import store.ApplicationStore;

import java.util.UUID;

@Slf4j
public class FileRequestHandler extends SimpleChannelInboundHandler<FileRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileRequest fileRequest) throws Exception {

        FileDirector fileDirector = new FileDirector(fileRequest.getFile().getPath() + "/");
        if (fileDirector.fileExists(fileRequest.getFile().getName())) {
            new Thread(() -> {
                UUID uuid = UUID.randomUUID();

                ApplicationStore.filesRequests.put(uuid, (FileResponse) -> {
                    synchronized (this) {
                        notify();
                    }
                });

                fileDirector.getFileDataParts(fileRequest.getFile().getName(), (FilePart part) -> {
                    synchronized (this) {
                        part.setUser(fileRequest.getUser());
                        part.setUuid(uuid);
                        channelHandlerContext.writeAndFlush(part);
                        if (part.isLast()) {
                            ApplicationStore.filesRequests.remove(uuid);
                        }
                        else {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                });
            }).start();
        }
        else {
            //TODO
        }
    }
}
