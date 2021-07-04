package file_tools;

import lombok.extern.slf4j.Slf4j;
import models.File;
import models.FilePart;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class FileDirector {
    private String rootPath;
    private static final int bufferSize = 524288;
    private Thread watchThread;
    private Consumer<WatchEvent<?>> watchKeyConsumer;
    private WatchService watchService;

    public FileDirector(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getFileText(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        if (Files.exists(path)) {
            try {
                StringBuilder sb = new StringBuilder();
                List<String> rows = Files.readAllLines(path, StandardCharsets.UTF_8);
                for (String row: rows) {
                    sb.append(row).append("\n\r");
                }
                return sb.toString();
            }
            catch (IOException e) {
                log.info(String.format("Cant read file %s%n", fileName), e);
            }
        }
        return null;
    }

    public boolean fileExists(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        return Files.exists(path);
    }

    public boolean isDirectory(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        return Files.isDirectory(path);
    }

    public LinkedList<File> getFilesInDirectory(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        LinkedList<File> fileList = new LinkedList<>();
        if (Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!path.equals(dir)) {
                            fileList.add(
                                    File.builder()
                                    .path(path.toString())
                                    .name(path.relativize(dir).toString())
                                    .type(File.DIR_TYPE)
                                    .build()
                            );
                        }
                        return !path.equals(dir) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        fileList.add(
                                File.builder()
                                .path(path.toString())
                                .name(path.relativize(file).toString())
                                .type(File.FILE_TYPE)
                                .build()
                        );
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            catch (IOException e) {
                log.info("Cant walk in directory " + fileName, e);
            }

        }
        return fileList;
    }

    public boolean createDirectory(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        try {
            Path directory = Files.createDirectory(path);
            return directory != null;
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public void getFileDataParts(String fileName, Consumer<FilePart> callback) {
        Path path = Paths.get(rootPath + fileName);
        if (Files.exists(path)) {
            try(RandomAccessFile accessFile = new RandomAccessFile(rootPath + fileName, "r"); FileChannel inChannel = accessFile.getChannel()) {
                long size = inChannel.size();
                int parts = (int) (inChannel.size() / bufferSize);
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
                int i = 1;
                while(inChannel.read(buffer) > 0)
                {
                    buffer.flip();
                    size = size - buffer.capacity();
                    byte[] array = buffer.array();
                    FilePart part = FilePart.builder()
                            .part(i)
                            .countParts(parts)
                            .name(fileName)
                            .build();
                    if (size <= 0) {
                        int currentCapacity = (int) (buffer.capacity() + size);
                        byte[] endArray = new byte[currentCapacity];
                        System.arraycopy(array, 0, endArray, 0, currentCapacity);
                        part.setByteArray(endArray);
                        part.setLast(true);
                    }
                    else {
                        part.setByteArray(array);
                    }

                    callback.accept(part);
                    buffer.clear();
                    i++;
                }
            }
            catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public boolean createFile(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        boolean res = false;
        if (!this.fileExists(fileName)) {
            try {
                Files.createFile(path);
                res = true;
            }
            catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return res;
    }

    public boolean deleteFile(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        boolean res = false;

        try {
            res = Files.deleteIfExists(path);
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return res;
    }

    public boolean setFileDataParts(String fileName, byte[] filePart) {
        Path path = Paths.get(rootPath + fileName);
        if (!this.fileExists(fileName)) {
            if (!this.createFile(fileName)) {
                return false;
            }
        }
        boolean res = false;
        try {
            Files.write(path, filePart, StandardOpenOption.APPEND);
            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public String getAbsolutePath(String fileName) {
        Path path = Paths.get(rootPath + fileName);
        return path.toAbsolutePath().toString();
    }

    public boolean updateFileName(String fileName, String newName) {
        Path path = Paths.get(rootPath + fileName);
        boolean res = false;

        try {
            path = Files.move(path, path.resolveSibling(newName));
            res = true;
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return res;
    }

    public WatchKey registerWatcher(String directoryPath) {
        try {
            if (watchService == null) {
                watchService = FileSystems.getDefault().newWatchService();
            }
            Path path = Paths.get(rootPath + directoryPath);
            WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            if (watchThread == null || !watchThread.isAlive()) {
                watchThread = new Thread(() -> {
                    WatchKey currentKey;
                    try {
                        while ((currentKey = watchService.take()) != null) {
                            for (WatchEvent<?> event : currentKey.pollEvents()) {
                                if (watchKeyConsumer != null) {
                                    watchKeyConsumer.accept(event);
                                }
                            }
                            currentKey.reset();
                        }
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                });
                watchThread.setDaemon(true);
                watchThread.start();
            }
            return key;

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void setWatchKeyConsumer(Consumer<WatchEvent<?>> consumer) {
        this.watchKeyConsumer = consumer;
    }

    public void closeKey(WatchKey key) {
        key.cancel();
    }

}
