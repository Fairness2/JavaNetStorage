import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NioServer {

    private final ServerSocketChannel socketChannel;
    private final Selector selector;
    private final DateTimeFormatter formatter;
    private final ClientHandler clientHandler;

    public NioServer() throws IOException {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
        socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.bind(new InetSocketAddress(8000));
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        clientHandler = new ClientHandler(selector, socketChannel);
    }

    public void run() {
        while (socketChannel.isOpen()) {
            try {
                selector.select();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                continue;
            }

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    clientHandler.accept(key);
                } else if (key.isReadable()) {
                    clientHandler.read(key);
                }
            }
        }
    }


}
