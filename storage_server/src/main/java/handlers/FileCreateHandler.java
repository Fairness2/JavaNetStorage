package handlers;

import database.FileDBManager;
import file_tools.FileDirector;
import helpers.Helpers;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.File;
import models.FileCreate;
import models.StandardAnswer;
import models.SynchronizeResponse;
import store.ApplicationStore;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FileCreateHandler extends SimpleChannelInboundHandler<FileCreate> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileCreate fileCreate) throws Exception {

        StandardAnswer answer;
        String directory = fileCreate.getFile().getPath() != null ? fileCreate.getFile().getPath() : (ApplicationStore.ROOT_PATH + fileCreate.getUser().getId());
        FileDirector fileDirector = new FileDirector( directory + "/");
        if (fileDirector.fileExists(fileCreate.getFile().getName())) {
            answer = StandardAnswer.builder()
                    .success(false)
                    .message(fileCreate.getFile().getType().equals(File.DIR_TYPE) ? "Директория уже существует" : "Файл уже существует")
                    .build();
        }
        else {
            boolean res = fileCreate.getFile().getType().equals(File.DIR_TYPE) ? fileDirector.createDirectory(fileCreate.getFile().getName()) : fileDirector.createFile(fileCreate.getFile().getName());
            LinkedList<File> fileList = (LinkedList<File>) Helpers.mergeSharedFile(fileDirector.getFilesInDirectory(""), directory, fileCreate.getUser().getId());

            answer = StandardAnswer.builder()
                    .success(res)
                    .message(res
                            ? (fileCreate.getFile().getType().equals(File.DIR_TYPE)
                                ? "Директория создана"
                                : "Файл создан")
                            : (fileCreate.getFile().getType().equals(File.DIR_TYPE)
                                ? "Создать директорию не удалось"
                                : "Создать файл не удалось"))
                    .param(
                            SynchronizeResponse.builder()
                                    .path(fileCreate.getFile().getPath())
                                    .fileList(fileList)
                                    .build()
                    )
                    .build();
        }

        channelHandlerContext.writeAndFlush(answer);
    }
}
