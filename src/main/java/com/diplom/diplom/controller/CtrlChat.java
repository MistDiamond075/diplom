package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOChat;
import com.diplom.diplom.entity.EntChat;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.chat.ServiceChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CtrlChat {
    private final ServiceChat srvChat;

    @Autowired
    public CtrlChat(ServiceChat srvChat) {
        this.srvChat = srvChat;
    }

    @GetMapping("/chat/{id}/getUsers")
    public @ResponseBody DTOChat getChatUsers(@PathVariable Long id,@RequestParam(value = "page",defaultValue = "0") int page,@AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvChat.getUsersInChat(id,page,userDetails);
    }

    @PostMapping("/chat/create/addChat")
    public @ResponseBody DTOChat addChat(@RequestBody DTOChat chat, @AuthenticationPrincipal UserDetails userDetails) throws EntityException, AccessException {
        return srvChat.addChat(chat,userDetails);
    }

    @DeleteMapping("/chats/deleteChat/{id}")
    public @ResponseBody DTOChat deleteChat(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvChat.deleteChat(id,userDetails);
    }

    @PatchMapping("chat/{id}/update/updateChat")
    public @ResponseBody DTOChat updateChat(@PathVariable Long id, @RequestBody DTOChat chat, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvChat.updateChat(id,chat,userDetails);
    }
}
