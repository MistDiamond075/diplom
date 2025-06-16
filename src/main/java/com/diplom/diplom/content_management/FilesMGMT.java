package com.diplom.diplom.content_management;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilesMGMT {
    private static String saveAsOriginal(String filepath,MultipartFile file,String name) {
        try {
            Path fullpath = Path.of(filepath,name);
            file.transferTo(fullpath);
            return fullpath.toString();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String saveTextFile(String filepath,String text,String name) throws IOException {
        Path fullpath = Path.of(filepath,name);
        Files.writeString(fullpath, text, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        return fullpath.toString();
    }

    public static List<String> saveFiles(MultipartFile[] files,String dirpath,Long task_id,Long user_id) {
        List<String> filepathList = new ArrayList<>();
        if(files.length>0) {
            String fullpath=dirpath+'/'+task_id+'/'+user_id;
            File filesdir = new File(fullpath);
            if (!filesdir.exists()) {
                if (!filesdir.mkdirs()) {
                    throw new RuntimeException("Ошибка обработки файлов");
                }
            }
            int s = 1;
            for (MultipartFile file : files) {
                String pathtofile=saveAsOriginal(fullpath,file,user_id+"_"+s+"_"+file.getOriginalFilename());
                filepathList.add(pathtofile);
                s++;
            }
        }
        return filepathList;
    }

    private String getFileExtension(MultipartFile file) {
        String contentType = file.getContentType();
         return contentType!=null ? contentType.substring(contentType.indexOf( "/")+1) : "";
    }
}
