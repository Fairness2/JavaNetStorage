package handlers;

import database.FileDBManager;
import file_tools.FileDirector;
import helpers.Helpers;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.File;
import models.FileDelete;
import models.StandardAnswer;
import models.SynchronizeResponse;
import store.ApplicationStore;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FileDeleteHandler extends SimpleChannelInboundHandler<FileDelete> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileDelete fileDelete) throws Exception {

        StandardAnswer answer;
        String directory = fileDelete.getFile().getPath() != null ? fileDelete.getFile().getPath() : (ApplicationStore.ROOT_PATH + fileDelete.getUser().getId());
        FileDirector fileDirector = new FileDirector(directory + "/");
        if (!fileDirector.fileExists(fileDelete.getFile().getName())) {
            answer = StandardAnswer.builder()
                    .success(false)
                    .message(fileDelete.getFile().getType().equals(File.DIR_TYPE) ? "Директория не существует" : "Файл не существует")
                    .build();
        }
        else {
            boolean res = fileDirector.deleteFile(fileDelete.getFile().getName());
            FileDBManager fileDBManager = new FileDBManager();
            if (fileDelete.getFile().getType().equals(File.DIR_TYPE)) {
                fileDBManager.deleteFile(fileDelete.getFile().getPath() + "/" + fileDelete.getFile().getName());
            }
            else {
                fileDBManager.deleteFile(fileDelete.getFile());
            }

            LinkedList<File> fileList = (LinkedList<File>) Helpers.mergeSharedFile(fileDirector.getFilesInDirectory(""), directory, fileDelete.getUser().getId());

            answer = StandardAnswer.builder()
                    .success(res)
                    .message(res
                            ? (fileDelete.getFile().getType().equals(File.DIR_TYPE)
                                ? "Директория удалёна"
                                : "Файл удалён")
                            : (fileDelete.getFile().getType().equals(File.DIR_TYPE)
                                ? "Удалить директорию не удалось"
                                : "Удалить файл не удалось"))
                    .param(
                            SynchronizeResponse.builder()
                                    .path(fileDelete.getFile().getPath())
                                    .fileList(fileList)
                                    .build()
                    )
                    .build();
        }

        channelHandlerContext.writeAndFlush(answer);
    }
}
