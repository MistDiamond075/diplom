package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="role",schema = "db_diplom")
public class EntRole {
@Id
@Column(name = "id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
@Column(name = "name")
  private String name;
@Column(name="power")
private Integer power;
@ManyToMany
@JoinTable(
        name = "role_has_user",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
)
@JsonIgnore
private List<EntUser> users;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<EntUser> getUsers() {
    return users;
  }

  public void setUsers(List<EntUser> users) {
    this.users = users;
  }

  public Integer getPower() {
    return power;
  }

  public void setPower(Integer power) {
    this.power = power;
  }
}
