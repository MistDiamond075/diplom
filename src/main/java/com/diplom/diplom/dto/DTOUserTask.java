package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntUser;

public class DTOUserTask {
    private EntUser user;
    private String groups;

    public DTOUserTask() {
    }

    public DTOUserTask(EntUser user, String groups) {
        this.user = user;
        this.groups = groups;
    }

    public EntUser getUser() {
        return user;
    }

    public void setUser(EntUser user) {
        this.user = user;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }
}
