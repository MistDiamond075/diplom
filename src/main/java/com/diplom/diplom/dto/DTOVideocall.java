package com.diplom.diplom.dto;

public class DTOVideocall {
    private Long id;
    Long roomId;
    Integer participants;
    private DTOConferences conferencesId;

    public DTOVideocall() {
    }

    public DTOVideocall(Long id, Long roomId, Integer participants, DTOConferences conferencesId) {
        this.id = id;
        this.roomId = roomId;
        this.participants = participants;
        this.conferencesId = conferencesId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public DTOConferences getConferencesId() {
        return conferencesId;
    }

    public void setConferencesId(DTOConferences conferencesId) {
        this.conferencesId = conferencesId;
    }
}
