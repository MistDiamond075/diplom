package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name="journal",schema = "db_diplom")
public class EntJournal {
@Id
@Column(name="id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@ManyToOne
@JoinColumn(name = "journaluser_id")
  private EntUser journaluserId;
@ManyToOne
@JoinColumn(name = "journalsubject_id")
  private EntSubject journalsubjectId;
  @Column(name = "date")
  private String date;
  @Column(name="grade")
  private Integer grade;
  @Column(name="waspresent")
  private Boolean waspresent;
  @ManyToOne
  @JoinColumn(name="tasks_completed_id")
  @JsonBackReference
  private EntTasksCompleted journaltasksCompletedId;

  public EntJournal() {
  }

  public EntJournal(Long id, EntUser journaluserId, EntSubject journalsubjectId, String date, Integer grade, Boolean waspresent) {
    this.id = id;
    this.journaluserId = journaluserId;
    this.journalsubjectId = journalsubjectId;
    this.date = date;
    this.grade = grade;
    this.waspresent = waspresent;
  }

  public EntJournal(Long id, EntUser journaluserId, EntSubject journalsubjectId, String date, Integer grade, Boolean waspresent, EntTasksCompleted journaltasksCompletedId) {
    this.id = id;
    this.journaluserId = journaluserId;
    this.journalsubjectId = journalsubjectId;
    this.date = date;
    this.grade = grade;
    this.waspresent = waspresent;
    this.journaltasksCompletedId = journaltasksCompletedId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public EntUser getJournaluserId() {
    return journaluserId;
  }

  public void setJournaluserId(EntUser userId) {
    this.journaluserId = userId;
  }

  public EntSubject getJournalsubjectId() {
    return journalsubjectId;
  }

  public void setJournalsubjectId(EntSubject subjectId) {
    this.journalsubjectId = subjectId;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public Integer getGrade() {
    return grade;
  }

  public void setGrade(Integer grade) {
    this.grade = grade;
  }

  public Boolean getWaspresent() {
    return waspresent;
  }

  public void setWaspresent(Boolean waspresent) {
    this.waspresent = waspresent;
  }

  public EntTasksCompleted getJournaltasksCompletedId() {
    return journaltasksCompletedId;
  }

  public void setJournaltasksCompletedId(EntTasksCompleted tasksCompletedId) {
    this.journaltasksCompletedId = tasksCompletedId;
  }

}
