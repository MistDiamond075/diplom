package com.diplom.diplom.service.chat;

import com.diplom.diplom.configuration.ConfPropsPaths;
import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.content_management.FilesMGMT;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.dto.converter.ConverterFileToEntityFile;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.FilesProcessor;
import com.diplom.diplom.repository.RepChatFiles;
import com.diplom.diplom.repository.RepChatMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceChatFiles {
    private final RepChatFiles rChatFiles;
    private final RepChatMessage rChatMessage;
    private final ConfPropsPaths app_paths;

    @Autowired
    public ServiceChatFiles(RepChatFiles rChatFiles, RepChatMessage rChatMessage, ConfPropsPaths appPaths) {
        this.rChatFiles = rChatFiles;
        this.rChatMessage = rChatMessage;
        app_paths = appPaths;
    }

    public List<DTOFile> getChatFilesByMessageId(Long messageId, DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntUser user=userDetails.getUser();
        EntChatMessage message=rChatMessage.findById(messageId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "message with id "+messageId+" not found",
                "Сообщение не найдено",
                EntChatMessage.class
        ));
        if(!isUserChatMember(user,message.getChatId())){
            throw new AccessException(
                    HttpStatus.FORBIDDEN,
                    "user tried to get files from other message",
                    "Ошибка доступа: вы не состоите в этом чате",
                    userDetails
            );
        }
        List<EntChatfiles> fileList= rChatFiles.findAllByMessageId(message);
        return !fileList.isEmpty() ? new ArrayList<>(fileList.stream().map(ConverterFileToEntityFile::convertChatFileToDTOFile).toList()) : new ArrayList<>();
    }

    public ResponseEntity<Resource> getFileForView(Long id,DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntUser user=userDetails.getUser();
        EntChatfiles file=rChatFiles.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "chat file with id "+id+" not found",
                "Файл не найден",
                EntChatfiles.class
        ));
       if(!isUserChatMember(user,file.getMessageId().getChatId())){
           throw new AccessException(
                   HttpStatus.FORBIDDEN,
                   "user tried to get files from other message",
                   "Ошибка доступа: вы не состоите в этом чате",
                   userDetails
           );
       }
        return FilesProcessor.getFileResource(file.getPath(), null);
    }

    @Transactional
    public void addFiles(MultipartFile[] files, EntChatMessage message) {
        List<String> file_paths= FilesMGMT.saveFiles(files,app_paths.getChatfiles(),message.getId(), null);
        for(String path:file_paths){
            EntChatfiles file=new EntChatfiles(null,path,message);
            rChatFiles.save(file);
        }
    }

    private boolean isUserChatMember(EntUser user,EntChat chat) {
        List<EntChatUser> chatUsers=chat.getChatUsers();
        return chatUsers.stream().map(EntChatUser::getUserId).toList().stream().anyMatch(user::equals);
    }
}
