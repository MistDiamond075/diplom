package com.diplom.diplom.service.chat;

import com.diplom.diplom.dto.DTOChatMessage;
import com.diplom.diplom.entity.EntChatMessage;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.Websocket.videocall.WebSocketVideocall;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@Service
@EnableAsync
public class ServiceChatMessageAsync {
    private final ServiceChatMessage srvChatMessage;

    @Autowired
    public ServiceChatMessageAsync(ServiceChatMessage srvChatMessage) {
        this.srvChatMessage = srvChatMessage;
    }

    public CompletableFuture< ResponseEntity<?>>addMessageToChat(Long chatId, Long replyId, EntChatMessage msg, MultipartFile[] files, UserDetails userDetails) throws AccessException, EntityException {
        DTOChatMessage dtoChatMessage = srvChatMessage.addMessageToChat(chatId,replyId,msg,files,userDetails);
        return CompletableFuture.supplyAsync(() -> {
            sendMessage(dtoChatMessage);
            return ResponseEntity.ok(dtoChatMessage);
        });
    }

    public CompletableFuture<ResponseEntity<?>> deleteMessageFromChat(Long messageId, UserDetails userDetails) throws AccessException, EntityException {
        DTOChatMessage dtoChatMessage=srvChatMessage.deleteMessageFromChat(messageId,userDetails);
        return CompletableFuture.supplyAsync(() ->{
            removeMessage(dtoChatMessage);
            return ResponseEntity.ok(dtoChatMessage);
        });
    }

    @Async
    public void sendMessage(DTOChatMessage dtoChatMessage){
        JSONObject userId = new JSONObject()
                .put("id",dtoChatMessage.getUserId().getId())
                .put("login",dtoChatMessage.getUserId().getLogin())
                .put("firstname",dtoChatMessage.getUserId().getFirstname())
                .put("lastname",dtoChatMessage.getUserId().getLastname())
                .put("surname",dtoChatMessage.getUserId().getSurname());
        JSONObject msg=new JSONObject()
                .put("id",dtoChatMessage.getId())
                .put("text",dtoChatMessage.getText())
                .put("date",dtoChatMessage.getDate())
                .put("chatId",dtoChatMessage.getChatId())
                .put("userId",userId)
                .put("replyTo",dtoChatMessage.getReplyTo());
        JSONObject response=new JSONObject()
                .put("event","chatmsg")
                .put("eventType","chat")
                .put("message",msg);
        WebSocketVideocall.sendMessageToChat(dtoChatMessage.getChatId(),response.toString() );
    }

    @Async
    public void removeMessage(DTOChatMessage dtoChatMessage){
        JSONObject response=new JSONObject()
                .put("event","removemsg")
                .put("eventType","chat")
                .put("messageId",dtoChatMessage.getId());
        WebSocketVideocall.sendMessageToChat(dtoChatMessage.getChatId(),response.toString() );
    }
}
