package com.diplom.diplom.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "password_restore_mails",schema = "db_diplom")
public class EntPasswordRestoreMails {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="datesend")
    private String datesend;

    @Column(name = "uuid")
    private String mailuuid;

    @OneToOne
    @JoinColumn(name = "user_id")
    private EntUser mailuserId;

    public EntPasswordRestoreMails() {
    }

    public EntPasswordRestoreMails(Long id, String datesend, String mailuuid, EntUser mailuserId) {
        this.id = id;
        this.datesend = datesend;
        this.mailuuid = mailuuid;
        this.mailuserId = mailuserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDatesend() {
        return datesend;
    }

    public void setDatesend(String datesend) {
        this.datesend = datesend;
    }

    public String getMailuuid() {
        return mailuuid;
    }

    public void setMailuuid(String mailuuid) {
        this.mailuuid = mailuuid;
    }

    public EntUser getMailuserId() {
        return mailuserId;
    }

    public void setMailuserId(EntUser mailuserId) {
        this.mailuserId = mailuserId;
    }
}
