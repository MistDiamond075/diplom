package com.diplom.diplom.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DTOChatMessage {
    private Long id;
    private String text;
    private LocalDateTime date;
    private Long chatId;
    private DTOUserUpdate userId;
    private Long replyTo;
    private List<DTOFile> files;

    public DTOChatMessage() {
    }

    public DTOChatMessage(Long id, String text, LocalDateTime date, Long chatId, DTOUserUpdate userId, Long replyTo, List<DTOFile> files) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.chatId = chatId;
        this.userId = userId;
        this.replyTo = replyTo;
        this.files = files;
    }

    public DTOChatMessage(Long id, String text, LocalDateTime date, Long chatId, DTOUserUpdate userId, Long replyTo) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.chatId = chatId;
        this.userId = userId;
        this.replyTo = replyTo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public DTOUserUpdate getUserId() {
        return userId;
    }

    public void setUserId(DTOUserUpdate userId) {
        this.userId = userId;
    }

    public Long getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Long replyTo) {
        this.replyTo = replyTo;
    }

    public List<DTOFile> getFiles() {
        return files;
    }

    public void setFiles(List<DTOFile> files) {
        this.files = files;
    }
}
