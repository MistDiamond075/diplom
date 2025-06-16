package com.diplom.diplom.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "userfiles",schema = "db_diplom")
public class EntUserfiles {
    public enum fileType{AVATAR,CSS,SETTINGS,OTHER}
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "path")
    private String path;

    @Enumerated(EnumType.STRING)
    private fileType type;

    @OneToOne
    @JoinColumn(name = "user_id")
    private EntUser filesuserId;

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

    public EntUser getFilesuserId() {
        return filesuserId;
    }

    public void setFilesuserId(EntUser filesuserId) {
        this.filesuserId = filesuserId;
    }

    public fileType getType() {
        return type;
    }

    public void setType(fileType type) {
        this.type = type;
    }

    public EntUserfiles(Long id, String path, EntUser filesuserId, fileType type) {
        this.id = id;
        this.path = path;
        this.filesuserId = filesuserId;
        this.type = type;
    }

    public EntUserfiles() {
    }
}
