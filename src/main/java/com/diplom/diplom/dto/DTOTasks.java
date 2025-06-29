package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntSubject;

import java.time.LocalDateTime;
import java.util.List;

public class DTOTasks {
    private Long id;
    private String name;
    private LocalDateTime datestart;
    private LocalDateTime dateend;
    private String text;
    private DTOUserUpdate createdby;
    private EntSubject tasksubjectId;
    private List<EntGroup> groups;

    public DTOTasks() {
    }

    public DTOTasks(Long id, String name, LocalDateTime datestart, LocalDateTime dateend, String text, DTOUserUpdate createdby, EntSubject tasksubjectId) {
        this.id = id;
        this.name = name;
        this.datestart = datestart;
        this.dateend = dateend;
        this.text = text;
        this.createdby = createdby;
        this.tasksubjectId = tasksubjectId;
    }

    public DTOTasks(Long id, String name, LocalDateTime datestart, LocalDateTime dateend, String text, DTOUserUpdate createdby, EntSubject tasksubjectId, List<EntGroup> groups) {
        this.id = id;
        this.name = name;
        this.datestart = datestart;
        this.dateend = dateend;
        this.text = text;
        this.createdby = createdby;
        this.tasksubjectId = tasksubjectId;
        this.groups = groups;
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

    public LocalDateTime getDatestart() {
        return datestart;
    }

    public void setDatestart(LocalDateTime datestart) {
        this.datestart = datestart;
    }

    public LocalDateTime getDateend() {
        return dateend;
    }

    public void setDateend(LocalDateTime dateend) {
        this.dateend = dateend;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DTOUserUpdate getCreatedby() {
        return createdby;
    }

    public void setCreatedby(DTOUserUpdate createdby) {
        this.createdby = createdby;
    }

    public EntSubject getTasksubjectId() {
        return tasksubjectId;
    }

    public void setTasksubjectId(EntSubject tasksubjectId) {
        this.tasksubjectId = tasksubjectId;
    }

    public List<EntGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<EntGroup> groups) {
        this.groups = groups;
    }
}
