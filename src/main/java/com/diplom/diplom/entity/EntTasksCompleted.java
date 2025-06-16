package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="tasks_completed",schema = "db_diplom")
public class EntTasksCompleted {
@Id
@Column(name="id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@ManyToOne
@JoinColumn(name = "tasks_id")
@JsonBackReference
  private EntTasks tasksId;
  @Column(name="dateofsubmit")
  private String dateofsubmit;
  @Column(name="grade")
  private Integer grade;
  @Column(name="commentary")
  private String commentary;
  @Column(name="feedback")
  private String feedback;
  @Column(name="dateofcheck")
  private String dateofcheck;
  @ManyToOne
  @JoinColumn(name = "user_id")
  private EntUser userId;
  @OneToMany(mappedBy = "journaltasksCompletedId",cascade = CascadeType.REMOVE)
  @JsonManagedReference
  private List<EntJournal> journal_list;
  @OneToMany(mappedBy = "tasksCompletedId",cascade = CascadeType.REMOVE)
  @JsonManagedReference
  private List<EntCompletedtasksfiles> files_list;

  public EntTasksCompleted(Long id, EntTasks tasksId, String dateofsubmit, Integer grade, String commentary, String feedback, String dateofcheck, EntUser userId) {
    this.id = id;
    this.tasksId = tasksId;
    this.dateofsubmit = dateofsubmit;
    this.grade = grade;
    this.commentary = commentary;
    this.feedback = feedback;
    this.dateofcheck = dateofcheck;
    this.userId = userId;
  }

  public EntTasksCompleted() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public EntTasks getTasksId() {
    return tasksId;
  }

  public void setTasksId(EntTasks tasksId) {
    this.tasksId = tasksId;
  }


  public String getDateofsubmit() {
    return dateofsubmit;
  }

  public void setDateofsubmit(String dateofsubmit) {
    this.dateofsubmit = dateofsubmit;
  }


  public Integer getGrade() {
    return grade;
  }

  public void setGrade(Integer grade) {
    this.grade = grade;
  }


  public String getCommentary() {
    return commentary;
  }

  public void setCommentary(String commentary) {
    this.commentary = commentary;
  }


  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public String getDateofcheck() {
    return dateofcheck;
  }

  public void setDateofcheck(String dateofcheck) {
    this.dateofcheck = dateofcheck;
  }

  public EntUser getUserId() {
    return userId;
  }

  public void setUserId(EntUser userId) {
    this.userId = userId;
  }

  public List<EntJournal> getJournal_list() {
    return journal_list;
  }

  public void setJournal_list(List<EntJournal> journal_list) {
    this.journal_list = journal_list;
  }

  public List<EntCompletedtasksfiles> getFiles_list() {
    return files_list;
  }

  public void setFiles_list(List<EntCompletedtasksfiles> files_list) {
    this.files_list = files_list;
  }
}
