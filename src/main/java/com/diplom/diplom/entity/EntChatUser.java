package com.diplom.diplom.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "chat_user",schema = "db_diplom")
public class EntChatUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id")
    private EntChat chatId;
    @ManyToOne(optional = false)
    @JoinColumn(name="user_id")
    private EntUser userId;

    public EntChatUser() {
    }

    public EntChatUser(Long id, EntChat chatId, EntUser userId) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntChat getChatId() {
        return chatId;
    }

    public void setChatId(EntChat entChat) {
        this.chatId = entChat;
    }

    public EntUser getUserId() {
        return userId;
    }

    public void setUserId(EntUser user) {
        this.userId = user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EntChatUser chatUser = (EntChatUser) o;
        return Objects.equals(id, chatUser.id) && Objects.equals(chatId, chatUser.chatId) && Objects.equals(userId, chatUser.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, userId);
    }
}
