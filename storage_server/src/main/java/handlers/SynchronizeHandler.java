package handlers;

import database.UserDBManager;
import file_tools.FileDirector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.*;
import store.ApplicationStore;

import java.sql.SQLException;
import java.util.LinkedList;

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
                String directory = String.valueOf(findUser.getId()) + '/' + (synchronizeRequest.getPath() == null ? "" : synchronizeRequest.getPath());
                FileDirector fileDirector = new FileDirector(ApplicationStore.ROOT_PATH);

                if (!fileDirector.isDirectory(String.valueOf(findUser.getId()))) {
                    fileDirector.createDirectory(String.valueOf(findUser.getId()));
                }

                if (fileDirector.fileExists(directory)) {

                    LinkedList<File> fileList = fileDirector.getFilesInDirectory(directory);
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
