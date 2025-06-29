package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntCompletedtasksfiles;

import java.util.List;

public class DTOCompletedTask {
    private Long id;
    private DTOTasks tasksId;
    private String dateofsubmit;
    private Integer grade;
    private String commentary;
    private String feedback;
    private String dateofcheck;
    private DTOUserUpdate userId;
    private List<EntCompletedtasksfiles> files_list;

    public DTOCompletedTask() {
    }

    public DTOCompletedTask(Long id, DTOTasks tasksId, String dateofsubmit, Integer grade, String commentary, String feedback, String dateofcheck, DTOUserUpdate userId, List<EntCompletedtasksfiles> files_list) {
        this.id = id;
        this.tasksId = tasksId;
        this.dateofsubmit = dateofsubmit;
        this.grade = grade;
        this.commentary = commentary;
        this.feedback = feedback;
        this.dateofcheck = dateofcheck;
        this.userId = userId;
        this.files_list = files_list;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DTOTasks getTasksId() {
        return tasksId;
    }

    public void setTasksId(DTOTasks tasksId) {
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

    public DTOUserUpdate getUserId() {
        return userId;
    }

    public void setUserId(DTOUserUpdate userId) {
        this.userId = userId;
    }

    public List<EntCompletedtasksfiles> getFiles_list() {
        return files_list;
    }

    public void setFiles_list(List<EntCompletedtasksfiles> files_list) {
        this.files_list = files_list;
    }
}
