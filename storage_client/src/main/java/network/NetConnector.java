package network;

import callbacks.Callback;
import handlers.MessageHandler;
import handlers.SignalHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import models.TransmittedSignal;

@Slf4j
public class NetConnector implements Runnable {
    private SocketChannel channel;
    private static NetConnector netConnector;

    public static NetConnector getInstance() {
        if (netConnector == null) {
            netConnector = new NetConnector();
        }
        return netConnector;
    }

    @Override
    public void run() {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            channel = socketChannel;
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new SignalHandler()
                            );
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("localhost",8000).sync();
            channelFuture.channel().closeFuture().sync(); //block
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            workGroup.shutdownGracefully();
        }
    }

    public void sendSignal(TransmittedSignal signal) {
        channel.writeAndFlush(signal);
    }
}
