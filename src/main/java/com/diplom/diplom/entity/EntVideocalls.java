package com.diplom.diplom.entity;

import jakarta.persistence.*;

@Entity
@Table(name="videocalls",schema = "db_diplom")
public class EntVideocalls {
@Id
@Column(name="id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@Column(name="room_id")
Long roomId;
@Column(name = "participants")
Integer participants;
@Column(name="session_id")
Long sessionId;
@Column(name="handle_id")
Long handleId;
@OneToOne
@JoinColumn(name="conferences_id")
  private EntConferences conferencesId;

  public EntVideocalls() {
  }

  public EntVideocalls(Long id, EntConferences conferencesId, Long roomId, Integer participants) {
    this.id = id;
    this.conferencesId = conferencesId;
    this.roomId = roomId;
    this.participants = participants;
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

  public EntConferences getConferencesId() {
    return conferencesId;
  }

  public void setConferencesId(EntConferences conferencesId) {
    this.conferencesId = conferencesId;
  }

  public Long getHandleId() {
    return handleId;
  }

  public void setHandleId(Long handleId) {
    this.handleId = handleId;
  }

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
  }
}


