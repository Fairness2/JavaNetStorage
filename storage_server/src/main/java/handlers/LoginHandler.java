package handlers;

import database.UserDBManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import models.LoginRequest;
import models.StandardAnswer;
import models.User;

import java.sql.SQLException;

@Slf4j
public class LoginHandler  extends SimpleChannelInboundHandler<LoginRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginRequest loginRequest) throws Exception {
        StandardAnswer answer;
        try {
            UserDBManager userManager = new UserDBManager();
            User findUser = userManager.findByCredentials(loginRequest.getLogin(), loginRequest.getPassword());

            if (findUser != null) {
                answer = StandardAnswer.builder()
                        .success(true)
                        .message("Пользователь верен")
                        .param(findUser)
                        .build();
            }
            else {
                answer = StandardAnswer.builder()
                        .success(false)
                        .message("Пользователь не найден")
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
