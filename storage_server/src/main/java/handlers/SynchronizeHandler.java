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
public class SynchronizeHandler extends SimpleChannelInboundHandler<SynchronizeRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SynchronizeRequest synchronizeRequest) throws Exception {
        StandardAnswer answer;

        User user = synchronizeRequest.getUser();

        try {
            UserDBManager userManager = new UserDBManager();
            User findUser = userManager.findById(user.getId());
            if (findUser == null) {
                answer = StandardAnswer.builder()
                        .success(false)
                        .message("Пользователь не верен")
                        .build();
            }
            else {
                FileDirector fileDirector = new FileDirector(ApplicationStore.ROOT_PATH);
                if (!fileDirector.isDirectory(String.valueOf(findUser.getId()))) {
                    fileDirector.createDirectory(String.valueOf(findUser.getId()));
                }
                String directory = synchronizeRequest.getPath() == null ? (ApplicationStore.ROOT_PATH + findUser.getId()) : (synchronizeRequest.getPath());
                fileDirector.setRootPath(directory + '/');

                if (fileDirector.fileExists("")) {
                    LinkedList<File> fileList = (LinkedList<File>) Helpers.mergeSharedFile(fileDirector.getFilesInDirectory(""), directory, user.getId());

                    answer = StandardAnswer.builder()
                            .success(true)
                            .param(
                                    SynchronizeResponse.builder()
                                            .path(synchronizeRequest.getPath())
                                            .fileList(fileList)
                                            .build()
                            )
                            .build();
                }
                else {
                    answer = StandardAnswer.builder()
                            .success(false)
                            .message("Директория не найдена")
                            .build();
                }
            }
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            answer = StandardAnswer.builder()
                    .success(false)
                    .message("Произошла ошибка")
                    .build();
        }

        channelHandlerContext.writeAndFlush(answer);
    }
}
