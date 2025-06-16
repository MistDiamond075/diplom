package com.diplom.diplom.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "subject",schema = "db_diplom")
public class EntSubject {
@Id
@Column(name="id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
@Column(name="name")
  private String name;
@OneToMany(mappedBy = "journalsubjectId")
  private List<EntJournal> journal_list;
@OneToMany(mappedBy = "tasksubjectId")
  private List<EntTasks> tasks_list;

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

}
