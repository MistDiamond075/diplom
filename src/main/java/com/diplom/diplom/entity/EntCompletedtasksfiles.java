package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name="completedtasksfiles",schema = "db_diplom")
public class EntCompletedtasksfiles {
@Id
@Column(name = "id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@Column(name = "path")
  private String path;
@ManyToOne
@JoinColumn(name = "task_id")
@JsonBackReference
  private EntTasksCompleted tasksCompletedId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }


  public EntTasksCompleted getTasksCompletedId() {
    return tasksCompletedId;
  }

  public void setTasksCompletedId(EntTasksCompleted tasksCompletedId) {
    this.tasksCompletedId = tasksCompletedId;
  }

}
