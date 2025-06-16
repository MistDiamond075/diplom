package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "videocall_has_user_properties",schema = "db_diplom")
public class EntVideocallsHasUserProperties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name = "micromuted")
    private int micromuted;
    @Column(name="cameramuted")
    private int cameramuted;
    @Column(name="demomuted")
    private int demomuted;
    @Column(name="soundmuted")
    private int soundmuted;
    @Column(name="banned")
    private boolean banned;
    @OneToOne
    @JoinColumn(name = "videocall_has_user_id")
    @JsonBackReference
    private EntVideocallsHasUser videocallHasUserId;

    public EntVideocallsHasUserProperties(Long id, EntVideocallsHasUser videocallHasUserId, int micromuted, int cameramuted, int demomuted, int soundmuted, boolean banned) {
        this.id = id;
        this.videocallHasUserId = videocallHasUserId;
        this.micromuted = micromuted;
        this.cameramuted = cameramuted;
        this.demomuted = demomuted;
        this.soundmuted = soundmuted;
        this.banned = banned;
    }

    public EntVideocallsHasUserProperties() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMicromuted() {
        return micromuted;
    }

    public void setMicromuted(int micromuted) {
        this.micromuted = micromuted;
    }

    public int getCameramuted() {
        return cameramuted;
    }

    public void setCameramuted(int cameramuted) {
        this.cameramuted = cameramuted;
    }

    public int getDemomuted() {
        return demomuted;
    }

    public void setDemomuted(int demomuted) {
        this.demomuted = demomuted;
    }

    public int getSoundmuted() {
        return soundmuted;
    }

    public void setSoundmuted(int soundmuted) {
        this.soundmuted = soundmuted;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public EntVideocallsHasUser getVideocallHasUserId() {
        return videocallHasUserId;
    }

    public void setVideocallHasUserId(EntVideocallsHasUser videocallHasUserId) {
        this.videocallHasUserId = videocallHasUserId;
    }
}
