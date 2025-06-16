package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "banlist",schema = "db_diplom")
public class EntBanlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="reason")
    private String reason;
    @Column(name="start")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime start;
    @Column(name="end")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime end;
    @Column(name="ipaddress")
    private String ipaddress;
    @ManyToOne
    @JoinColumn(name="user_id")
    private EntUser userId;
    @ManyToOne
    @JoinColumn(name = "bannedby")
    private EntUser bannedBy;

    public EntBanlist() {
    }

    public EntBanlist(Long id, String reason, LocalDateTime start, LocalDateTime end, EntUser userId, EntUser bannedBy, String ipaddress) {
        this.id = id;
        this.reason = reason;
        this.start = start;
        this.end = end;
        this.userId = userId;
        this.bannedBy = bannedBy;
        this.ipaddress = ipaddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public EntUser getUserId() {
        return userId;
    }

    public void setUserId(EntUser userId) {
        this.userId = userId;
    }

    public EntUser getBannedBy() {
        return bannedBy;
    }

    public void setBannedBy(EntUser bannedBy) {
        this.bannedBy = bannedBy;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }
}
