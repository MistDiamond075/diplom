package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOVideocallChatMessage;
import com.diplom.diplom.service.videocalls.ServiceVideocallsChatAsync;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
public class CtrlVideocallChat {
    private final ServiceVideocallsChatAsync srvVideocallChatAsync;

    public CtrlVideocallChat(ServiceVideocallsChatAsync srvVideocallChatAsync) {
        this.srvVideocallChatAsync = srvVideocallChatAsync;
    }

    @PostMapping("/videocall/{id}/addMessage")
    public CompletableFuture<ResponseEntity<?>> addMessage(@PathVariable Long id, @RequestBody DTOVideocallChatMessage text, @AuthenticationPrincipal DiplomUserDetails userDetails) {
        return srvVideocallChatAsync.addMessage(id,text.getText(),text.getReplyTo(),userDetails);
    }
}
