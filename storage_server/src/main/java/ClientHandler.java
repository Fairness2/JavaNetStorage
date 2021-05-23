import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

@Slf4j
public class ClientHandler {
    private final Selector selector;
    private final ServerSocketChannel socketChannel;
    private static String helpAnswer = "Allowed commands: ls, cat, help\n\r";
    private final FileHandler fileHandler;

    public ClientHandler(Selector selector, ServerSocketChannel socketChannel) {
        this.selector = selector;
        this.socketChannel = socketChannel;
        fileHandler = new FileHandler();
    }

    public void accept(SelectionKey key) {
        try {
            SocketChannel channel = socketChannel.accept();
            if (channel != null) {
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);
            }
        }
        catch (IOException e) {
            log.info("Error accept", e);
        }
    }

    public void read(SelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(256);

        int r = 0;

        try {
            while ((r = channel.read(buffer)) != 0){
                if (r == -1) {
                    channel.close();
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    sb.append((char) buffer.get());
                }
            }
        }
        catch (IOException e) {
            log.info("Client disconnected", e);
        }

        if (sb.isEmpty()) {
            sendMessage("Command is empty", key);
            return;
        }
        doCommand(sb.toString(), key);
    }

    public void sendBroadcastMessage(String message) {
        for (SelectionKey key : selector.keys()) {
            sendMessage(message, key);
        }
    }

    public void sendMessage(String message, SelectionKey key) {
        if (key.isValid() && key.channel() instanceof SocketChannel) {
            SocketChannel ch = (SocketChannel) key.channel();
            try {
                ch.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            }
            catch (IOException e) {
                log.info("Error to send message", e);
            }
        }
    }

    public void doCommand(String command, SelectionKey key) {
        command = command.replaceAll("(\\r|\\n)", "");
        if (command.startsWith("help")) {
            sendMessage(helpAnswer, key);
        }
        else if (command.startsWith("ls")) {
            String dirName = command.replaceFirst("ls", "").trim();
            if (fileHandler.isDirectory(dirName)) {
                LinkedList<String> fileList = fileHandler.getFilesInDirectory(dirName);
                StringBuilder sb = new StringBuilder();
                for (String file: fileList) {
                    sb.append(file).append("\n\r");
                }
                sendMessage(sb.toString(), key);
            }
            else {
                sendMessage("It is not directory\n\r", key);
            }
        }
        else if (command.startsWith("cat")) {
            String fileName = command.replaceFirst("cat", "").trim();
            if (fileHandler.fileExists(fileName)) {
                sendMessage(fileHandler.getFileText(fileName), key);
            }
            else {
                sendMessage("File does not exist\n\r", key);
            }
        }
        else {
            sendMessage("Command does not exist\n\r", key);
        }
    }


}
