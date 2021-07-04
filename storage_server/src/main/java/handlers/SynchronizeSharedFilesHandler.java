package handlers;

import database.FileDBManager;
import database.UserDBManager;
import file_tools.FileDirector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.*;
import store.ApplicationStore;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class SynchronizeSharedFilesHandler extends SimpleChannelInboundHandler<SynchronizeSharedFilesRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SynchronizeSharedFilesRequest synchronizeRequest) throws Exception {
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
                FileDBManager fileDBManager = new FileDBManager();

                List<File> fileList = fileDBManager.getSharedFiles(user.getId());
                answer = StandardAnswer.builder()
                        .success(true)
                        .param(
                                SynchronizeResponse.builder()
                                        .path(null)
                                        .fileList(fileList)
                                        .build()
                        )
                        .build();
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
