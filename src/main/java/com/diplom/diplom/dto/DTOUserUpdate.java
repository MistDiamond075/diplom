package com.diplom.diplom.dto;

import java.util.List;

public class DTOUserUpdate {
    private Long id;
    private String login;
    private String password;
    private String firstname;
    private String lastname;
    private String surname;
    private String dateofbirth;
    private String email;
    private String qwestion;
    private String qwestionanswer;
    private String studentcard;
    private List<String> groupsNames;
    private List<String> userRoles;

    public DTOUserUpdate(Long id, String login, String firstname, String lastname, String surname, String dateofbirth, String email, String qwestion, String qwestionanswer, String studentcard, List<String> groupsNames, List<String> userRoles) {
        this.id = id;
        this.login = login;
        this.firstname = firstname;
        this.lastname = lastname;
        this.surname = surname;
        this.dateofbirth = dateofbirth;
        this.email = email;
        this.qwestion = qwestion;
        this.qwestionanswer = qwestionanswer;
        this.studentcard = studentcard;
        this.groupsNames = groupsNames;
        this.userRoles = userRoles;
    }

    public DTOUserUpdate(Long id, String login, String firstname, String lastname, String surname) {
        this.id = id;
        this.login = login;
        this.firstname = firstname;
        this.lastname = lastname;
        this.surname = surname;
    }

    public DTOUserUpdate(Long id, String login, String firstname, String lastname, String surname, List<String> groupsNames) {
        this.id = id;
        this.login = login;
        this.firstname = firstname;
        this.lastname = lastname;
        this.surname = surname;
        this.groupsNames = groupsNames;
    }

    public DTOUserUpdate() {
    }

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

    public String getQwestion() {
        return qwestion;
    }

    public void setQwestion(String qwestion) {
        this.qwestion = qwestion;
    }

    public String getQwestionanswer() {
        return qwestionanswer;
    }

    public void setQwestionanswer(String qwestionanswer) {
        this.qwestionanswer = qwestionanswer;
    }

    public String getStudentcard() {
        return studentcard;
    }

    public void setStudentcard(String studentcard) {
        this.studentcard = studentcard;
    }

    public List<String> getGroupsNames() {
        return groupsNames;
    }

    public void setGroupsNames(List<String> groupsNames) {
        this.groupsNames = groupsNames;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<String> userRoles) {
        this.userRoles = userRoles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
