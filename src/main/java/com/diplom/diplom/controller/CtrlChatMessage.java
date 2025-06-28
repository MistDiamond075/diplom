package com.diplom.diplom.controller;

import com.diplom.diplom.dto.DTOChatMessage;
import com.diplom.diplom.entity.EntChatMessage;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.chat.ServiceChatMessageAsync;
import com.diplom.diplom.service.chat.ServiceChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class CtrlChatMessage {
    private final ServiceChatMessage srvChatMessage;
    private final ServiceChatMessageAsync srvChatAsync;

    @Autowired
    public CtrlChatMessage(ServiceChatMessage srvChatMessage, ServiceChatMessageAsync srvChatAsync) {
        this.srvChatMessage = srvChatMessage;
        this.srvChatAsync = srvChatAsync;
    }

    @GetMapping("/getMessages/{chatid}")
    public @ResponseBody List<DTOChatMessage> getMessagesInChat(@PathVariable Long chatid, @RequestParam(value = "page",defaultValue = "0") int page, @AuthenticationPrincipal UserDetails userDetails) throws EntityException, AccessException {
        return srvChatMessage.getMessagesInChat(chatid,page,userDetails);
    }

    @PostMapping(path = "/addMessage/{chatid}",consumes = {"multipart/form-data"})
    public @ResponseBody CompletableFuture<ResponseEntity<?>> addMessage(
            @PathVariable Long chatid,
            @RequestParam(value="replyTo",required = false) Long replyId,
            @RequestPart(value = "senddata") EntChatMessage entChatMessage,
            @RequestPart(value = "files",required = false)MultipartFile[] files,
            @AuthenticationPrincipal UserDetails userDetails) throws EntityException, AccessException {
        return srvChatAsync.addMessageToChat(chatid,replyId,entChatMessage,files,userDetails);
    }

    @DeleteMapping("/deleteMessage/{msgId}")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> deleteMessage(@PathVariable Long msgId, @AuthenticationPrincipal UserDetails userDetails) throws EntityException, AccessException {
        return srvChatAsync.deleteMessageFromChat(msgId,userDetails);
    }
}
