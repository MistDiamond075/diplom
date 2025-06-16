package com.diplom.diplom.entity;

import jakarta.persistence.*;

@Entity
@Table(name="chatfiles",schema = "db_diplom")
public class EntChatfiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="path")
    private String path;
    @ManyToOne
    @JoinColumn(name = "message_id")
    private EntChatMessage messageId;

    public EntChatfiles() {
    }

    public EntChatfiles(Long id, String path, EntChatMessage messageId) {
        this.id = id;
        this.path = path;
        this.messageId = messageId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public EntChatMessage getMessageId() {
        return messageId;
    }

    public void setMessageId(EntChatMessage messageId) {
        this.messageId = messageId;
    }
}
