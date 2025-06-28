package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.entity.EntChatfiles;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.chat.ServiceChatFiles;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CtrlChatfiles {
    private final ServiceChatFiles srvChatFiles;

    public CtrlChatfiles(ServiceChatFiles srvChatFiles) {
        this.srvChatFiles = srvChatFiles;
    }

    @GetMapping("/chats/{chatId}/message/{msgId}/getFiles")
    public List<DTOFile> getChatFilesByMessageId(@PathVariable Long msgId, @AuthenticationPrincipal DiplomUserDetails userDetails) throws EntityException, AccessException {
        return srvChatFiles.getChatFilesByMessageId(msgId,userDetails);
    }

    @GetMapping("/chats/{chatId}/message/{msgId}/file/{id}/view")
    public ResponseEntity<Resource> getFile(@PathVariable Long id,@AuthenticationPrincipal DiplomUserDetails userDetails) throws EntityException, AccessException {
        return srvChatFiles.getFileForView(id,userDetails);
    }

}
