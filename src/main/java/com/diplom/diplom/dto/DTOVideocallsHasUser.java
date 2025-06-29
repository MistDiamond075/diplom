package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntVideocallsHasUser;

public class DTOVideocallsHasUser {
    private Long id;
    private DTOVideocall videocallsId;
    private DTOUserUpdate videocalluserId;
    private EntVideocallsHasUser.defaultStates microstate;
    private EntVideocallsHasUser.defaultStates camstate;
    private EntVideocallsHasUser.defaultStates soundstate;
    private EntVideocallsHasUser.defaultStates demostate;
    private Boolean signalstate;
    private Boolean connected;

    public DTOVideocallsHasUser() {
    }

    public DTOVideocallsHasUser(Long id, DTOVideocall videocallsId, DTOUserUpdate videocalluserId, EntVideocallsHasUser.defaultStates microstate, EntVideocallsHasUser.defaultStates camstate, EntVideocallsHasUser.defaultStates soundstate, EntVideocallsHasUser.defaultStates demostate, Boolean signalstate, Boolean connected) {
        this.id = id;
        this.videocallsId = videocallsId;
        this.videocalluserId = videocalluserId;
        this.microstate = microstate;
        this.camstate = camstate;
        this.soundstate = soundstate;
        this.demostate = demostate;
        this.signalstate = signalstate;
        this.connected = connected;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DTOVideocall getVideocallsId() {
        return videocallsId;
    }

    public void setVideocallsId(DTOVideocall videocallsId) {
        this.videocallsId = videocallsId;
    }

    public DTOUserUpdate getVideocalluserId() {
        return videocalluserId;
    }

    public void setVideocalluserId(DTOUserUpdate videocalluserId) {
        this.videocalluserId = videocalluserId;
    }

    public EntVideocallsHasUser.defaultStates getMicrostate() {
        return microstate;
    }

    public void setMicrostate(EntVideocallsHasUser.defaultStates microstate) {
        this.microstate = microstate;
    }

    public EntVideocallsHasUser.defaultStates getCamstate() {
        return camstate;
    }

    public void setCamstate(EntVideocallsHasUser.defaultStates camstate) {
        this.camstate = camstate;
    }

    public EntVideocallsHasUser.defaultStates getSoundstate() {
        return soundstate;
    }

    public void setSoundstate(EntVideocallsHasUser.defaultStates soundstate) {
        this.soundstate = soundstate;
    }

    public EntVideocallsHasUser.defaultStates getDemostate() {
        return demostate;
    }

    public void setDemostate(EntVideocallsHasUser.defaultStates demostate) {
        this.demostate = demostate;
    }

    public Boolean getSignalstate() {
        return signalstate;
    }

    public void setSignalstate(Boolean signalstate) {
        this.signalstate = signalstate;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }
}
