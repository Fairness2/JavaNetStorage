package file_tools;

import lombok.extern.slf4j.Slf4j;
import models.File;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FileDirector {
    private String rootPath;

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
}
