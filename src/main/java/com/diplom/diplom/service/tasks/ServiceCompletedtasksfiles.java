package com.diplom.diplom.service.tasks;

import com.diplom.diplom.configuration.ConfPropsPaths;
import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.content_management.FilesMGMT;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.dto.converter.ConverterFileToEntityFile;
import com.diplom.diplom.entity.EntCompletedtasksfiles;
import com.diplom.diplom.entity.EntTasksCompleted;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.misc.utils.FilesProcessor;
import com.diplom.diplom.repository.RepCompletedtasksfiles;
import com.diplom.diplom.repository.RepTasksCompleted;
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
public class ServiceCompletedtasksfiles {
    private final RepCompletedtasksfiles rCompletedtasksfiles;
    private final RepTasksCompleted rTasksCompleted;
    private final RepUser rUser;
    //private final FilesMGMT completedTasksMGMT;
    private final ConfPropsPaths app_paths;

    @Autowired
    public ServiceCompletedtasksfiles(RepCompletedtasksfiles rCompletedtasksfiles, RepTasksCompleted rTasksCompleted, RepUser rUser, FilesMGMT completedTasksMGMT, ConfPropsPaths appPaths) {
        this.rCompletedtasksfiles = rCompletedtasksfiles;
        this.rTasksCompleted = rTasksCompleted;
        this.rUser = rUser;
        //this.completedTasksMGMT = completedTasksMGMT;
        app_paths = appPaths;
    }

    public List<DTOFile> getCompletedtasksfiles(){
        List<EntCompletedtasksfiles> fileList= (List<EntCompletedtasksfiles>) rCompletedtasksfiles.findAll();
        List<DTOFile> files = new ArrayList<>();
        for(EntCompletedtasksfiles file : fileList){
            files.add(ConverterFileToEntityFile.convertCompletedTaskFileToDTOFile(file));
        }
        return files;
    }

    public DTOFile getCompletedtaskFile(Long id) throws EntityException {
        EntCompletedtasksfiles file=rCompletedtasksfiles.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "completed task file with id "+id+" not found",
                "Файл не найден",
                EntCompletedtasksfiles.class
        ));
        return ConverterFileToEntityFile.convertCompletedTaskFileToDTOFile(file);
    }

    public ResponseEntity<List<Map<String, String>>> getCompletedTaskFileForFrontend(Long taskId, UserDetails userDetails){
        EntTasksCompleted tasksCompleted=rTasksCompleted.findById(taskId).orElseThrow(()->new RuntimeException("completed task not found when getting task files by task id "+taskId));
        List<EntCompletedtasksfiles> files= (List<EntCompletedtasksfiles>) rCompletedtasksfiles.findAllByTasksCompletedId(tasksCompleted);
        List<Map<String, String>> resources = files.stream()
                .map(file -> {
                    Map<String,String> jsmap=new HashMap<>();
                    FilesProcessor.getFileResource(file.getPath().replace(File.separator,"/"),null).getBody();
                    jsmap.put("id",file.getId().toString());
                    jsmap.put("url",file.getPath());
                    jsmap.put("type",file.getPath().substring(file.getPath().lastIndexOf('.')+1));
                    jsmap.put("name",file.getPath().substring(file.getPath().lastIndexOf(File.separator)+1,file.getPath().lastIndexOf('.')));
                    jsmap.put("user_id", file.getTasksCompletedId().getUserId().getId().toString());
                    return jsmap;
                })
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(resources);
    }

    public ResponseEntity<Resource> getCompletedTaskFileForView(Long taskId) throws EntityException {
        EntCompletedtasksfiles file=rCompletedtasksfiles.findById(taskId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "completed task file with id "+taskId+" not found",
                "Файл не найден",
                EntCompletedtasksfiles.class
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
    public void addFiles(MultipartFile[] files, EntTasksCompleted completed_task, Long user_id) {
        List<String> file_paths=FilesMGMT.saveFiles(files,app_paths.getTaskscompletedfiles(),completed_task.getTasksId().getId(),user_id);
        for(String path:file_paths){
            EntCompletedtasksfiles task=new EntCompletedtasksfiles();
            task.setId(null);
            task.setTasksCompletedId(completed_task);
            task.setPath(path);
            rCompletedtasksfiles.save(task);
        }
    }

    @Transactional
    public EntCompletedtasksfiles deleteByFileId(Long fileId, DiplomUserDetails userDetails) throws AccessException {
        EntCompletedtasksfiles file=rCompletedtasksfiles.findById(fileId).orElseThrow(()->new RuntimeException("completed task file not found when getting file id "+fileId));
        if(!Checker.checkUserIdentity(userDetails,file.getTasksCompletedId().getTasksId().getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.UNAUTHORIZED,"user identity check failed","Ошибка проверки данных аккаунта",userDetails);
        }
        rCompletedtasksfiles.delete(file);
        FilesProcessor.deleteFiles(file.getPath());
        return file;
    }
}
