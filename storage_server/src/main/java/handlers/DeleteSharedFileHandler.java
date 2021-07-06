package handlers;

import database.FileDBManager;
import database.UserDBManager;
import file_tools.FileDirector;
import helpers.Helpers;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.*;
import store.ApplicationStore;

import java.util.LinkedList;

@Slf4j
public class DeleteSharedFileHandler extends SimpleChannelInboundHandler<DeleteSharedFile> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DeleteSharedFile deleteSharedFile) throws Exception {
        StandardAnswer answer;

        FileDBManager fileDBManager = new FileDBManager();
        int res;
        if (deleteSharedFile.getFile().getType().equals(File.DIR_TYPE)) {
            res = fileDBManager.deleteFile(deleteSharedFile.getFile().getPath() + "/" + deleteSharedFile.getFile().getName());
        }
        else {
            res = fileDBManager.deleteFile(deleteSharedFile.getFile());
        }

        if (res > 0) {
            String directory = (deleteSharedFile.getFile().getPath() != null ? deleteSharedFile.getFile().getPath() : (ApplicationStore.ROOT_PATH + deleteSharedFile.getUser().getId()));
            FileDirector fileDirector = new FileDirector( directory + "/");
            LinkedList<File> fileList = (LinkedList<File>) Helpers.mergeSharedFile(fileDirector.getFilesInDirectory(""), directory, deleteSharedFile.getUser().getId());

            answer = StandardAnswer.builder()
                    .success(true)
                    .message("Файл закрыт")
                    .param(SynchronizeResponse.builder()
                            .path(deleteSharedFile.getFile().getPath())
                            .fileList(fileList)
                            .build())
                    .build();
        }
        else {
            answer = StandardAnswer.builder()
                    .success(false)
                    .message("Закрыть файл не удалось")
                    .build();
        }

        channelHandlerContext.writeAndFlush(answer);
    }
}
