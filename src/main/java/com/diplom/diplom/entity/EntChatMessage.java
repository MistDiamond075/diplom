package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="chat_message",schema = "db_diplom")
public class EntChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="text")
    private String text;
    @Column(name="date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime date;
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private EntChat chatId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private EntUser user;
    @ManyToOne
    @JoinColumn(name="replyto")
    private EntChatMessage replyTo;

    public EntChatMessage() {
    }

    public EntChatMessage(Long id, String text, LocalDateTime date, EntChat chatId, EntUser user, EntChatMessage replyTo) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.chatId = chatId;
        this.user = user;
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

    public EntChat getChatId() {
        return chatId;
    }

    public void setChatId(EntChat entChat) {
        this.chatId = entChat;
    }

    public EntUser getUser() {
        return user;
    }

    public void setUser(EntUser user) {
        this.user = user;
    }

    public EntChatMessage getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(EntChatMessage replyTo) {
        this.replyTo = replyTo;
    }
}
