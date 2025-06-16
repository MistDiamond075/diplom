package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntVideocallsHasUser;

public class DTOVideocallUpdate {
    private EntVideocallsHasUser videocallsHasUser;
    private Boolean isBanned;
    private EntVideocallsHasUser.defaultStates state;

    public DTOVideocallUpdate(EntVideocallsHasUser videocallsHasUser, Boolean isBanned, EntVideocallsHasUser.defaultStates state) {
        this.videocallsHasUser = videocallsHasUser;
        this.isBanned = isBanned;
        this.state = state;
    }

    public EntVideocallsHasUser getVideocallsHasUser() {
        return videocallsHasUser;
    }

    public void setVideocallsHasUser(EntVideocallsHasUser videocallsHasUser) {
        this.videocallsHasUser = videocallsHasUser;
    }

    public Boolean getBanned() {
        return isBanned;
    }

    public void setBanned(Boolean banned) {
        isBanned = banned;
    }

    public EntVideocallsHasUser.defaultStates getState() {
        return state;
    }

    public void setState(EntVideocallsHasUser.defaultStates state) {
        this.state = state;
    }
}
