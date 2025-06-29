package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntSubject;

import java.time.LocalDateTime;
import java.util.List;

public class DTOConferences {
    private Long id;
    private String name;
    private LocalDateTime datestart;
    private LocalDateTime dateend;
    private Integer repeatable;
    private List<EntGroup> groupId;
    private EntSubject subjectId;
    private DTOUserUpdate createdby;

    public DTOConferences() {
    }

    public DTOConferences(Long id, String name, LocalDateTime datestart, LocalDateTime dateend, Integer repeatable, List<EntGroup> groupId, EntSubject subjectId, DTOUserUpdate createdby) {
        this.id = id;
        this.name = name;
        this.datestart = datestart;
        this.dateend = dateend;
        this.repeatable = repeatable;
        this.groupId = groupId;
        this.subjectId = subjectId;
        this.createdby = createdby;
    }

    public DTOConferences(Long id, String name, LocalDateTime datestart, LocalDateTime dateend, Integer repeatable, EntSubject subjectId, DTOUserUpdate createdby) {
        this.id = id;
        this.name = name;
        this.datestart = datestart;
        this.dateend = dateend;
        this.repeatable = repeatable;
        this.subjectId = subjectId;
        this.createdby = createdby;
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

    public Integer getRepeatable() {
        return repeatable;
    }

    public void setRepeatable(Integer repeatable) {
        this.repeatable = repeatable;
    }

    public List<EntGroup> getGroupId() {
        return groupId;
    }

    public void setGroupId(List<EntGroup> groupId) {
        this.groupId = groupId;
    }

    public EntSubject getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(EntSubject subjectId) {
        this.subjectId = subjectId;
    }

    public DTOUserUpdate getCreatedby() {
        return createdby;
    }

    public void setCreatedby(DTOUserUpdate createdby) {
        this.createdby = createdby;
    }
}
