package com.diplom.diplom.service.videocalls;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOMessageVideocall;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.misc.Websocket.videocall.WebSocketVideocall;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@EnableAsync
public class ServiceVideocallsChatAsync {
    private final ServiceVideocallsChat srvVideocallsChat;

    @Autowired
    public ServiceVideocallsChatAsync(ServiceVideocallsChat srvVideocallsChat) {
        this.srvVideocallsChat = srvVideocallsChat;
    }

    @Async
    public CompletableFuture<ResponseEntity<?>> addMessage(Long videocallId,String message_text,Long replyTo, DiplomUserDetails userDetails) throws AccessException {
        DTOMessageVideocall message= srvVideocallsChat.addMessage(videocallId,message_text,replyTo, userDetails);
        return CompletableFuture.supplyAsync(() -> {
                    sendMessage(message);
                    return message;
                })
                .thenApply(ResponseEntity::ok)
                .handle((result, ex) ->{
                    if (ex == null) return result;
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AccessException ae) {
                        return ResponseEntity.status(ae.getStatus())
                                .body(new JSONObject()
                                        .put("message",ae.getMsg_for_user())
                                        .put("userRole",ae.getUserRole()));
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new JSONObject().put("error","unrecognized"));
                });
    }

    @Async
    public void sendMessage(DTOMessageVideocall message) {
        JSONObject msg=new JSONObject()
                .put("event","chatmsg")
                .put("eventType","videocall")
                .put("id",message.getId())
                .put("text",message.getMessage())
                .put("timestamp",message.getDate())
                .put("username",message.getVideocalluserId().getVideocalluserId().getLastname()+" "+message.getVideocalluserId().getVideocalluserId().getFirstname()+" "+message.getVideocalluserId().getVideocalluserId().getSurname())
                .put("login",message.getVideocalluserId().getVideocalluserId().getLogin())
                .put("replyToId",message.getReplyTo()==null ? JSONObject.NULL : message.getReplyTo())
                .put("replyToName",message.getReplyToName()==null ? JSONObject.NULL : message.getReplyToName());
        WebSocketVideocall.broadcastParticipants(message.getVideocalluserId().getVideocallsId().getRoomId(),msg.toString());
    }
}
