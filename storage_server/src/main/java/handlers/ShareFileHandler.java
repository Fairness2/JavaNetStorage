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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ShareFileHandler extends SimpleChannelInboundHandler<ShareFile> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ShareFile shareFile) throws Exception {
        StandardAnswer answer;

        FileDBManager fileDBManager = new FileDBManager();
        UserDBManager userDBManager = new UserDBManager();

        User sharedUser = userDBManager.findByLogin(shareFile.getUserLogin());
        if (sharedUser != null) {
            Integer fileId;
            if (!shareFile.getFile().isShared()) {
                fileId = fileDBManager.createFile(shareFile.getFile());
            }
            else {
                fileId = shareFile.getFile().getFileId();
            }
            Integer res = fileDBManager.shareFile(fileId, sharedUser.getId());
            if (res != null) {
                String directory = (shareFile.getFile().getPath() != null ? shareFile.getFile().getPath() : (ApplicationStore.ROOT_PATH + shareFile.getUser().getId()));
                FileDirector fileDirector = new FileDirector( directory + "/");
                LinkedList<File> fileList = (LinkedList<File>) Helpers.mergeSharedFile(fileDirector.getFilesInDirectory(""), directory, shareFile.getUser().getId());

                answer = StandardAnswer.builder()
                        .success(true)
                        .message("Вы поделились файлом")
                        .param(SynchronizeResponse.builder()
                                .path(shareFile.getFile().getPath())
                                .fileList(fileList)
                                .build())
                        .build();
            }
            else {
                answer = StandardAnswer.builder()
                        .success(false)
                        .message("Поделится файлом не удалось")
                        .build();
            }

        }
        else {
            answer = StandardAnswer.builder()
                    .success(false)
                    .message("Пользователь не найден")
                    .build();
        }

        channelHandlerContext.writeAndFlush(answer);
    }
}
