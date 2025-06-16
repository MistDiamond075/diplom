package com.diplom.diplom.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class VideocallsHasUserId implements Serializable {
    private Long conferenceId;
    private Long userId;

    public VideocallsHasUserId(Long conferenceId, Long userId) {
        this.conferenceId = conferenceId;
        this.userId = userId;
    }

    public VideocallsHasUserId() {
    }

    public Long getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(Long conferenceId) {
        this.conferenceId = conferenceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
