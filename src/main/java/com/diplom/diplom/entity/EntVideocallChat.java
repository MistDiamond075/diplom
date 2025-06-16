package com.diplom.diplom.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="videocall_chat",schema = "db_diplom")
public class EntVideocallChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="message")
    private String message;
    @Column(name = "date")
    private LocalDateTime date;
    @ManyToOne
    @JoinColumn(name = "videocalluser_id")
    private EntVideocallsHasUser videocalluserId;
    @ManyToOne
    @JoinColumn(name="replyto")
    private EntVideocallsHasUser replyto;

    public EntVideocallChat() {
    }

    public EntVideocallChat(Long id, String message, LocalDateTime date, EntVideocallsHasUser videocalluserId, EntVideocallsHasUser replyto) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.videocalluserId = videocalluserId;
        this.replyto = replyto;
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

    public EntVideocallsHasUser getVideocalluserId() {
        return videocalluserId;
    }

    public void setVideocalluserId(EntVideocallsHasUser videocalluserId) {
        this.videocalluserId = videocalluserId;
    }

    public EntVideocallsHasUser getReplyto() {
        return replyto;
    }

    public void setReplyto(EntVideocallsHasUser replyto) {
        this.replyto = replyto;
    }
}
