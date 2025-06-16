package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "videocalls_has_user",schema = "db_diplom",uniqueConstraints = @UniqueConstraint(columnNames = {"videocalls_id", "videocalluser_id"}))
public class EntVideocallsHasUser {
  public enum defaultStates{ON,OFF,MUTED_BY_ADMIN}
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@ManyToOne
@JoinColumn(name = "videocalls_id")
  private EntVideocalls videocallsId;
  @ManyToOne
  @JoinColumn(name = "videocalluser_id")
  private EntUser videocalluserId;
  @Enumerated(EnumType.STRING)
  private defaultStates microstate;
  @Enumerated(EnumType.STRING)
  private defaultStates camstate;
  @Enumerated(EnumType.STRING)
  private defaultStates soundstate;
  @Enumerated(EnumType.STRING)
  private defaultStates demostate;
  @Column(name = "signalstate")
  private Boolean signalstate;
  @Column(name="connected")
  private Boolean connected;
  @OneToOne(mappedBy = "videocallHasUserId", cascade = CascadeType.ALL, orphanRemoval = true)
  private EntVideocallsHasUserProperties properties;
  @OneToMany(mappedBy = "videocalluserId",cascade = CascadeType.ALL,orphanRemoval = true)
  @JsonIgnore
  private List<EntVideocallChat> chatMessage;

  public EntVideocallsHasUser(Long id, EntVideocalls videocallsId, EntUser videocalluserId, defaultStates microstate, defaultStates camstate, defaultStates soundstate, defaultStates demostate, Boolean signalstate, Boolean connected) {
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

  public EntVideocallsHasUser() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public EntVideocalls getVideocallsId() {
    return videocallsId;
  }

  public void setVideocallsId(EntVideocalls videocallsId) {
    this.videocallsId = videocallsId;
  }


  public EntUser getVideocalluserId() {
    return videocalluserId;
  }

  public void setVideocalluserId(EntUser userId) {
    this.videocalluserId = userId;
  }


  public defaultStates getMicrostate() {
    return microstate;
  }

  public void setMicrostate(defaultStates microstate) {
    this.microstate = microstate;
  }


  public defaultStates getCamstate() {
    return camstate;
  }

  public void setCamstate(defaultStates camstate) {
    this.camstate = camstate;
  }

  public defaultStates getSoundstate() {
    return soundstate;
  }

  public void setSoundstate(defaultStates soundstate) {
    this.soundstate = soundstate;
  }

  public defaultStates getDemostate() {
    return demostate;
  }

  public void setDemostate(defaultStates demostate) {
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

  public EntVideocallsHasUserProperties getProperties() {
    return properties;
  }

  public void setProperties(EntVideocallsHasUserProperties properties) {
    this.properties = properties;
  }

  public List<EntVideocallChat> getChatMessage() {
    return chatMessage;
  }

  public void setChatMessage(List<EntVideocallChat> chatMessage) {
    this.chatMessage = chatMessage;
  }
}
