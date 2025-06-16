package com.diplom.diplom.service.tasks;

import com.diplom.diplom.configuration.ConfPropsPaths;
import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.content_management.FilesMGMT;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.dto.converter.ConverterFileToEntityFile;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.misc.utils.FilesProcessor;
import com.diplom.diplom.repository.RepTasks;
import com.diplom.diplom.repository.RepTasksFiles;
import com.diplom.diplom.repository.RepUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceTasksfiles {
    private final RepTasksFiles rTasksfiles;
    private final RepTasks rTasks;
    private final RepUser rUser;
    private final ConfPropsPaths app_paths;

    @Autowired
    public ServiceTasksfiles(RepTasksFiles rTasksfiles, RepTasks rTasks, RepUser rUser, ConfPropsPaths appPaths) {
        this.rTasksfiles = rTasksfiles;
        this.rTasks = rTasks;
        this.rUser = rUser;
        app_paths = appPaths;
    }

    public List<DTOFile> getTasksfiles(){
        List<EntTasksfiles> fileList= (List<EntTasksfiles>) rTasksfiles.findAll();
        List<DTOFile> files= new ArrayList<>();
        for(EntTasksfiles file : fileList){
            files.add(ConverterFileToEntityFile.convertTaskFileToDTOFile(file));
        }
        return files;
    }

    public DTOFile getTasksfile(Long id) throws EntityException {
        EntTasksfiles file= rTasksfiles.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task file with id "+id+" not found",
                "Файл не найден",
                EntTasksfiles.class
        ));
        return ConverterFileToEntityFile.convertTaskFileToDTOFile(file);
    }

    @Transactional
    public void addFiles(MultipartFile[] files, EntTasks newtask, EntUser user) {
        List<String> file_paths= FilesMGMT.saveFiles(files,app_paths.getTasksfiles(),newtask.getId(), user.getId());
        for(String path:file_paths){
            EntTasksfiles task=new EntTasksfiles(null,newtask ,path);
            rTasksfiles.save(task);
        }
    }

    public ResponseEntity<List<Map<String, String>>> getTaskFilesForFrontend(Long taskId,UserDetails userDetails) throws EntityException {
        EntTasks task=rTasks.findById(taskId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task not found when getting task files by task id "+taskId,
                "Задание не найдено",
                EntTasks.class
        ));
        List<EntTasksfiles> files= rTasksfiles.findAllByTaskId(task);
        List<Map<String, String>> resources = files.stream()
                .map(file -> {
                    Map<String,String> jsmap=new HashMap<>();
                    FilesProcessor.getFileResource(file.getPath().replace(File.separator,"/"),null).getBody();
                    jsmap.put("id",file.getId().toString());
                    jsmap.put("url",file.getPath());
                    jsmap.put("type",file.getPath().substring(file.getPath().lastIndexOf('.')+1));
                    jsmap.put("name",file.getPath().substring(file.getPath().lastIndexOf(File.separator)+1,file.getPath().lastIndexOf('.')));
                    jsmap.put("user_id", file.getTaskId().getCreatedby().getId().toString());
                    return jsmap;
                })
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(resources);
    }

    public ResponseEntity<Resource> getTaskFileForView(Long id) throws EntityException {
        EntTasksfiles file=rTasksfiles.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task with id "+id+" not found",
                "Задание не найдено",
                EntTasks.class
        ));
        try {
            Path filePath = Paths.get(file.getPath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public EntTasksfiles deleteByFileId(Long id, DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntTasksfiles file=rTasksfiles.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task file not found when getting file by id "+id,
                "Файл не найден",
                EntTasksfiles.class
        ));
        if(!Checker.checkUserIdentity(userDetails,file.getTaskId().getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        rTasksfiles.delete(file);
        FilesProcessor.deleteFiles(file.getPath());
        return file;
    }
}
