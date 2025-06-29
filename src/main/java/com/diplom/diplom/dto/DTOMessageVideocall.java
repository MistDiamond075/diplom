package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntVideocallsHasUser;

import java.time.LocalDateTime;

public class DTOMessageVideocall {
    private Long id;
    private String message;
    private LocalDateTime date;
    private DTOVideocallsHasUser videocalluserId;
    private Long replyTo;
    private String replyToName;

    public DTOMessageVideocall(Long id, String message, LocalDateTime date, DTOVideocallsHasUser videocalluserId, Long replyTo, String replyToName) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.videocalluserId = videocalluserId;
        this.replyTo = replyTo;
        this.replyToName = replyToName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public DTOVideocallsHasUser getVideocalluserId() {
        return videocalluserId;
    }

    public void setVideocalluserId(DTOVideocallsHasUser videocalluserId) {
        this.videocalluserId = videocalluserId;
    }

    public Long getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Long replyTo) {
        this.replyTo = replyTo;
    }

    public String getReplyToName() {
        return replyToName;
    }

    public void setReplyToName(String replyToName) {
        this.replyToName = replyToName;
    }
}
