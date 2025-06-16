package com.diplom.diplom.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "chat",schema = "db_diplom")
public class EntChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="name")
    private String name;
    @ManyToOne
    @JoinColumn(name="subjectid")
    private EntSubject subjectId;
    @ManyToOne
    @JoinColumn(name="createdby")
    private EntUser createBy;
    @ManyToOne
    @JoinColumn(name="groupid")
    private EntGroup groupId;
    @OneToMany(mappedBy = "chatId",cascade = CascadeType.ALL)
    private List<EntChatUser> chatUsers;
    @OneToMany(mappedBy = "chatId",cascade = CascadeType.ALL)
    private List<EntChatMessage> chatMessages;

    public EntChat(Long id, String name, EntSubject subjectId, EntUser createBy, EntGroup groupId) {
        this.id = id;
        this.name = name;
        this.subjectId = subjectId;
        this.createBy = createBy;
        this.groupId = groupId;
    }

    public EntChat() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntSubject getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(EntSubject subjectId) {
        this.subjectId = subjectId;
    }

    public EntUser getCreateBy() {
        return createBy;
    }

    public void setCreateBy(EntUser createBy) {
        this.createBy = createBy;
    }

    public EntGroup getGroupId() {
        return groupId;
    }

    public void setGroupId(EntGroup groupId) {
        this.groupId = groupId;
    }

    public List<EntChatUser> getChatUsers() {
        return chatUsers;
    }

    public void setChatUsers(List<EntChatUser> chatUsers) {
        this.chatUsers = chatUsers;
    }

    public List<EntChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<EntChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }
}
