package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="tasks",schema = "db_diplom")
public class EntTasks {
@Id
@Column(name = "id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@Column(name = "name")
  private String name;
@Column(name="datestart")
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime datestart;
  @Column(name="dateend")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime dateend;
  @Column(name="text")
  private String text;
  @ManyToMany
  @JoinTable(
          name = "tasks_has_group",
          joinColumns = @JoinColumn(name = "tasks_id"),
          inverseJoinColumns = @JoinColumn(name = "group_id")
  )
  @JsonIgnore
  private List<EntGroup> groups;
  @ManyToOne
  @JoinColumn(name = "createdby")
  private EntUser createdby;
  @ManyToOne
  @JoinColumn(name="tasksubject_id")
  private EntSubject tasksubjectId;
  @OneToMany(mappedBy = "taskId",cascade = CascadeType.REMOVE)
  @JsonManagedReference
  private List<EntTasksfiles> taskfiles_list;
  @OneToMany(mappedBy = "tasksId",cascade = CascadeType.REMOVE)
  @JsonManagedReference
  private List<EntTasksCompleted> completedtasks_list;

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


  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }


  public EntUser getCreatedby() {
    return createdby;
  }

  public void setCreatedby(EntUser createdby) {
    this.createdby = createdby;
  }


  public EntSubject getTasksubjectId() {
    return tasksubjectId;
  }

  public void setTasksubjectId(EntSubject subjectId) {
    this.tasksubjectId = subjectId;
  }

  public List<EntGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<EntGroup> groups) {
    this.groups = groups;
  }

  public List<EntTasksfiles> getTaskfiles_list() {
    return taskfiles_list;
  }

  public void setTaskfiles_list(List<EntTasksfiles> taskfiles_list) {
    this.taskfiles_list = taskfiles_list;
  }

  public List<EntTasksCompleted> getCompletedtasks_list() {
    return completedtasks_list;
  }

  public void setCompletedtasks_list(List<EntTasksCompleted> completedtasks_list) {
    this.completedtasks_list = completedtasks_list;
  }
}
