package com.diplom.diplom.dto;

import org.springframework.http.MediaType;

public class DTOFile {
    private Long id;
    private String path;
    private String href;
    private Long parentEntityId;
    private MediaType fileType;

    public DTOFile(Long id, String path,String href, Long parentEntityId) {
        this.id = id;
        this.path = path;
        this.href = href;
        this.parentEntityId = parentEntityId;
    }

    public DTOFile(Long id, String path, String href, Long parentEntityId, MediaType fileType) {
        this.id = id;
        this.path = path;
        this.href = href;
        this.parentEntityId = parentEntityId;
        this.fileType = fileType;
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

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Long getParentEntityId() {
        return parentEntityId;
    }

    public void setParentEntityId(Long parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public MediaType getFileType() {
        return fileType;
    }

    public void setFileType(MediaType fileType) {
        this.fileType = fileType;
    }
}
