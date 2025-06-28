package com.diplom.diplom.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfPropsPaths {
    @Value("${path.userfilesfolder}")
    private String userfilesfolder;

    @Value("${path.taskscompletedfiles}")
    private String taskscompletedfiles;

    @Value("${path.tasksfiles}")
    private String tasksfiles;

    @Value("${path.chatfiles}")
    private String chatfiles;

    public String getTaskscompletedfiles() {
        return taskscompletedfiles;
    }

    public String getTasksfiles() {
        return tasksfiles;
    }

    public String getUserfilesfolder() {
        return userfilesfolder;
    }

    public String getChatfiles() {
        return chatfiles;
    }
}
