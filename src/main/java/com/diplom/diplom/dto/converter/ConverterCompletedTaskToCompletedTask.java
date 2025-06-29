package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOCompletedTask;
import com.diplom.diplom.dto.DTOTasks;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.entity.EntTasksCompleted;

public class ConverterCompletedTaskToCompletedTask {
    public static DTOCompletedTask convertEntityToDTO(EntTasksCompleted task){
        return new DTOCompletedTask(
                task.getId(),
                ConverterTasksToTasks.convertEntityToDTOwithGroups(task.getTasksId()),
                task.getDateofsubmit(),
                task.getGrade(),
                task.getCommentary(),
                task.getFeedback(),
                task.getDateofcheck(),
                new DTOUserUpdate(
                        task.getUserId().getId(),
                        task.getUserId().getLogin(),
                        task.getUserId().getFirstname(),
                        task.getUserId().getLastname(),
                        task.getUserId().getSurname()
                ),
                task.getFiles_list()
        );
    }
}
