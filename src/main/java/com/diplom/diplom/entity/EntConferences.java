package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="conferences",schema = "db_diplom")
public class EntConferences {
@Id
@Column(name = "id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@Column(name = "name")
  private String name;
@Column(name = "datestart")
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime datestart;
@Column(name="dateend")
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime dateend;
@Column(name = "repeatable")
private Integer repeatable;
@ManyToMany
@JoinTable(
        name = "group_has_conferences",
        joinColumns = @JoinColumn(name = "conferences_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id"))
@JsonIgnore
  private List<EntGroup> groupId;
@ManyToOne
@JoinColumn(name = "subject_id")
  private EntSubject subjectId;
@ManyToOne
@JoinColumn(name="createdby")
  private EntUser createdby;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDateTime getDatestart() {
    return datestart;
  }

  public void setDatestart(LocalDateTime datestart) {
    this.datestart = datestart;
  }

  public LocalDateTime getDateend() {
    return dateend;
  }

  public void setDateend(LocalDateTime dateend) {
    this.dateend = dateend;
  }

  public Integer getRepeatable() {
    return repeatable;
  }

  public void setRepeatable(Integer repeatperiod) {
    this.repeatable = repeatperiod;
  }

  public List<EntGroup> getGroupId() {
    return groupId;
  }

  public void setGroupId(List<EntGroup> groupId) {
    this.groupId = groupId;
  }

  public EntSubject getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(EntSubject subjectId) {
    this.subjectId = subjectId;
  }

  public EntUser getCreatedby() {
    return createdby;
  }

  public void setCreatedby(EntUser createdby) {
    this.createdby = createdby;
  }

}
