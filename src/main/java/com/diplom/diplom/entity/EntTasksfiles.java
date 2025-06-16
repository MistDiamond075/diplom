package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name="tasksfiles",schema = "db_diplom")
public class EntTasksfiles {
@Id
@Column(name = "id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@ManyToOne
@JsonBackReference
@JoinColumn(name = "task_id")
  private EntTasks taskId;
  @Column(name="path")
  private String path;

  public EntTasksfiles(Long id, EntTasks taskId, String path) {
    this.id = id;
    this.taskId = taskId;
    this.path = path;
  }

  public EntTasksfiles() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public EntTasks getTaskId() {
    return taskId;
  }

  public void setTaskId(EntTasks tasksId) {
    this.taskId = tasksId;
  }


  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

}
