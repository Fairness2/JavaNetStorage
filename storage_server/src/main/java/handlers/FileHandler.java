package handlers;

import file_tools.FileDirector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.*;
import store.ApplicationStore;

@Slf4j
public class FileHandler extends SimpleChannelInboundHandler<FilePart> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FilePart filePartRequest) throws Exception {
        FileResponse answer;

        String directory = filePartRequest.getPath() == null ? (ApplicationStore.ROOT_PATH + filePartRequest.getUser().getId()) : (filePartRequest.getPath());
        FileDirector fileDirector = new FileDirector(directory + "/");

        boolean res;
        if (filePartRequest.getPart() == 1) {
            fileDirector.deleteFile(filePartRequest.getName());
        }
        res = fileDirector.setFileDataParts(filePartRequest.getName(), filePartRequest.getByteArray());

        answer = FileResponse.builder()
                .status(res)
                .build();

        channelHandlerContext.writeAndFlush(answer);
    }
}
