package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntChat;

import java.util.List;

public class DTOChatDisplay {
    private EntChat chat;
    private List<DTOUserUpdate> user;

    public DTOChatDisplay(EntChat chat, List<DTOUserUpdate> user) {
        this.chat = chat;
        this.user = user;
    }

    public DTOChatDisplay() {
    }

    public EntChat getChat() {
        return chat;
    }

    public void setChat(EntChat chat) {
        this.chat = chat;
    }

    public List<DTOUserUpdate> getUser() {
        return user;
    }

    public void setUser(List<DTOUserUpdate> user) {
        this.user = user;
    }
}
