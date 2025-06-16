package com.diplom.diplom.dto;

import java.util.List;

public class DTOChat {
    private Long id;
    private String name;
    private String subjectName;
    private String groupName;
    private String[] members;
    private List<DTOUserUpdate> membersList;

    public DTOChat(Long id, String name, String subjectName, String groupName, String[] members) {
        this.id = id;
        this.name = name;
        this.subjectName = subjectName;
        this.groupName = groupName;
        this.members = members;
    }

    public DTOChat(Long id, String name, String subjectName, String groupName, List<DTOUserUpdate> membersList) {
        this.id = id;
        this.name = name;
        this.subjectName = subjectName;
        this.groupName = groupName;
        this.membersList = membersList;
    }

    public DTOChat() {
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

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String[] getMembers() {
        return members;
    }

    public void setMembers(String[] members) {
        this.members = members;
    }

    public List<DTOUserUpdate> getMembersList() {
        return membersList;
    }

    public void setMembersList(List<DTOUserUpdate> membersList) {
        this.membersList = membersList;
    }
}
