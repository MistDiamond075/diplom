package com.diplom.diplom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name="user",schema = "db_diplom")
public class EntUser {
  public enum Qwestion {PETNAME,MOMLNAME,RESERVEPW}
@Id
@Column(name = "id")
@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
@Column(name="login")
  private String login;
  @Column(name="password")
  private String password;
  @Column(name="firstname")
  private String firstname;
  @Column(name="lastname")
  private String lastname;
  @Column(name="surname")
  private String surname;
  @Column(name="dateofbirth")
  private String dateofbirth;
  @Column(name="email")
  private String email;
  @Enumerated(EnumType.STRING)
  private Qwestion qwestion;
  @Column(name="qwestionanswer")
  private String qwestionanswer;
  @Column(name="studentcard")
  private String studentcard;
  @ManyToMany
  @JoinTable(
          name="group_has_user",
          joinColumns = @JoinColumn(name="user_id"),
          inverseJoinColumns = @JoinColumn(name="group_id")
  )
  @JsonIgnore
  private List<EntGroup> groups;
  @OneToMany(mappedBy = "createdby",cascade = CascadeType.REMOVE)
  @JsonIgnore
  private List<EntTasks> created_tasks;
  @OneToMany(mappedBy = "userId",cascade = CascadeType.REMOVE)
  @JsonIgnore
  private List<EntTasksCompleted> completed_tasks;
  @OneToMany(mappedBy = "journaluserId",cascade = CascadeType.REMOVE)
  @JsonIgnore
  private List<EntJournal> journal_list;
  @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<EntChatUser> chats ;
  @ManyToMany
  @JoinTable(
          name = "role_has_user",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "role_id")
  )
@JsonIgnore
  private List<EntRole> roles;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }


  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }


  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }


  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }


  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }


  public String getDateofbirth() {
    return dateofbirth;
  }

  public void setDateofbirth(String dateofbirth) {
    this.dateofbirth = dateofbirth;
  }


  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }


  public Qwestion getQwestion() {
    return qwestion;
  }

  public void setQwestion(Qwestion qwestion) {
    this.qwestion = qwestion;
  }


  public String getQwestionanswer() {
    return qwestionanswer;
  }

  public void setQwestionanswer(String qwestionanswer) {
    this.qwestionanswer = qwestionanswer;
  }


  public List<EntGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<EntGroup> groupId) {
    this.groups = groupId;
  }

  public List<EntTasks> getCreated_tasks() {
    return created_tasks;
  }

  public void setCreated_tasks(List<EntTasks> created_tasks) {
    this.created_tasks = created_tasks;
  }

  public List<EntTasksCompleted> getCompleted_tasks() {
    return completed_tasks;
  }

  public void setCompleted_tasks(List<EntTasksCompleted> completed_tasks) {
    this.completed_tasks = completed_tasks;
  }

  public List<EntJournal> getJournal_list() {
    return journal_list;
  }

  public void setJournal_list(List<EntJournal> journal_list) {
    this.journal_list = journal_list;
  }

  public List<EntRole> getRoles() {
    return roles;
  }

  public void setRoles(List<EntRole> roles) {
    this.roles = roles;
  }

  public String getStudentcard() {
    return studentcard;
  }

  public void setStudentcard(String studentcard) {
    this.studentcard = studentcard;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    EntUser entUser = (EntUser) o;
    return Objects.equals(id, entUser.id) && Objects.equals(login, entUser.login) && Objects.equals(password, entUser.password) && Objects.equals(firstname, entUser.firstname) && Objects.equals(lastname, entUser.lastname) && Objects.equals(surname, entUser.surname) && Objects.equals(dateofbirth, entUser.dateofbirth) && Objects.equals(email, entUser.email) && qwestion == entUser.qwestion && Objects.equals(qwestionanswer, entUser.qwestionanswer) && Objects.equals(studentcard, entUser.studentcard);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, login, password, firstname, lastname, surname, dateofbirth, email, qwestion, qwestionanswer, studentcard);
  }
}
