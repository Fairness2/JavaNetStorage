package handlers;

import database.FileDBManager;
import file_tools.FileDirector;
import helpers.Helpers;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.*;
import store.ApplicationStore;

import java.util.LinkedList;

@Slf4j
public class FileUpdateHandler extends SimpleChannelInboundHandler<FileUpdate> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileUpdate fileUpdate) throws Exception {

        StandardAnswer answer;
        String directory = (fileUpdate.getFile().getPath() != null ? fileUpdate.getFile().getPath() : (ApplicationStore.ROOT_PATH + fileUpdate.getUser().getId()));
        FileDirector fileDirector = new FileDirector( directory + "/");
        if (!fileDirector.fileExists(fileUpdate.getFile().getName())) {
            answer = StandardAnswer.builder()
                    .success(false)
                    .message(fileUpdate.getFile().getType().equals(File.DIR_TYPE) ? "Директория не существует" : "Файл не существует")
                    .build();
        }
        else if (fileDirector.fileExists(fileUpdate.getNewName())) {
            answer = StandardAnswer.builder()
                    .success(false)
                    .message(fileUpdate.getFile().getType().equals(File.DIR_TYPE) ? "директория с новым именем уже существует" : "Файл с новым именем уже существует")
                    .build();
        }
        else {
            boolean res = fileDirector.updateFileName(fileUpdate.getFile().getName(), fileUpdate.getNewName());
            if (fileUpdate.getFile().getFileId() != null && fileUpdate.getFile().getFileId() != 0) {
                FileDBManager fileDBManager = new FileDBManager();
                fileDBManager.updateFile(fileUpdate.getFile().getFileId(), fileUpdate.getNewName());
            }


            LinkedList<File> fileList = (LinkedList<File>) Helpers.mergeSharedFile(fileDirector.getFilesInDirectory(""), directory, fileUpdate.getUser().getId());
            answer = StandardAnswer.builder()
                    .success(res)
                    .message(res
                            ? (fileUpdate.getFile().getType().equals(File.DIR_TYPE)
                                ? "Директория переименована"
                                : "Файл переименован")
                            : (fileUpdate.getFile().getType().equals(File.DIR_TYPE)
                                ? "Переименовать директорию не удалось"
                                : "Переименовать файл не удалось"))
                    .param(
                            SynchronizeResponse.builder()
                                    .path(fileUpdate.getFile().getPath())
                                    .fileList(fileList)
                                    .build()
                    )
                    .build();
        }

        channelHandlerContext.writeAndFlush(answer);
    }
}
