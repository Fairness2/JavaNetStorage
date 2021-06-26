import handlers.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {
    public NettyServer() {
        EventLoopGroup auth = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new LoginHandler(),
                                    new SynchronizeHandler(),
                                    new FileHandler(),
                                    new FileRequestHandler(),
                                    new FileResponseHandler()
                            );
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(8000).sync();
            log.debug("Server started...");
            channelFuture.channel().closeFuture().sync(); //block
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
