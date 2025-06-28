package com.diplom.diplom.misc.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FilesProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FilesProcessor.class);

    public static ResponseEntity<Resource> getFileResource(String filepath, HttpServletRequest request) {
        try {
            Path path = Paths.get(filepath);
            Resource fileResource = new FileSystemResource(path.toFile());
            if (!fileResource.exists()) {
                return ResponseEntity.notFound().build();
            }
            MediaType contentType =Parser.parseFileContentType(path);
            String etag = Generator.generateETag(fileResource.getFile());
            if(request!=null) {
                String ifNoneMatch = request.getHeader("If-None-Match");
                if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).cacheControl(CacheControl.maxAge(3,TimeUnit.DAYS)).eTag(etag).build();
                }
            }
            return ResponseEntity.ok().contentType(contentType).eTag(etag).body(fileResource);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    public static void deleteDirectory(String path){
        CompletableFuture.runAsync(() -> {
            try {
                final Path dir = Path.of(path);
                if (Files.exists(dir) && Files.isDirectory(dir)) {
                    Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.deleteIfExists(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path imgdir, IOException exc) throws IOException {
                            Files.deleteIfExists(imgdir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            } catch (IOException | RuntimeException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void deleteFiles(String path){
        CompletableFuture.runAsync(() -> {
            try {
                final Path imgdir = Path.of(path);
                if (Files.exists(imgdir) && Files.isDirectory(imgdir)) {
                    Files.walkFileTree(imgdir, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.deleteIfExists(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
