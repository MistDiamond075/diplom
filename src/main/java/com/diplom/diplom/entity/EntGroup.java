package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="`group`",schema = "db_diplom")
public class EntGroup {
@Id
@Column(name = "id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@Column(name = "name")
  private String name;
@Column(name="private")
  private Boolean privateGroup;
  @ManyToMany
  @JoinTable(
          name="group_has_user",
          joinColumns = @JoinColumn(name="group_id"),
          inverseJoinColumns = @JoinColumn(name="user_id")
  )
@JsonIgnore
private List<EntUser> users_list;
@ManyToMany
@JoinTable(
        name = "tasks_has_group",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "tasks_id")
)
@JsonIgnore
private List<EntTasks> tasks;
@ManyToMany
@JoinTable(
          name = "group_has_conferences",
          joinColumns = @JoinColumn(name = "group_id"),
          inverseJoinColumns = @JoinColumn(name = "conferences_id"))
@JsonIgnore
  private List<EntConferences> conferences;

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

  public Boolean getPrivateGroup() {
    return privateGroup;
  }

  public void setPrivateGroup(Boolean course) {
    this.privateGroup = course;
  }

  public List<EntUser> getUsers_list() {
    return users_list;
  }

  public void setUsers_list(List<EntUser> users_list) {
    this.users_list = users_list;
  }

  public List<EntTasks> getTasks() {
    return tasks;
  }

  public void setTasks(List<EntTasks> tasks) {
    this.tasks = tasks;
  }

  public List<EntConferences> getConferences() {
    return conferences;
  }

  public void setConferences(List<EntConferences> conferences) {
    this.conferences = conferences;
  }
}
