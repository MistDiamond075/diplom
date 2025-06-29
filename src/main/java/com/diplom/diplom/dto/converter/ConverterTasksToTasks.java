package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOTasks;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.entity.EntTasks;
import org.springframework.scheduling.config.Task;

public class ConverterTasksToTasks {
    public static DTOTasks convertEntityToDTO(EntTasks task) {
        return new DTOTasks(
           task.getId(),
                task.getName(),
                task.getDatestart(),
                task.getDateend(),
                task.getText(),
                new DTOUserUpdate(
                        task.getCreatedby().getId(),
                        task.getCreatedby().getLogin(),
                        task.getCreatedby().getFirstname(),
                        task.getCreatedby().getLastname(),
                        task.getCreatedby().getSurname()
                ),
                task.getTasksubjectId()
        );
    }

    public static DTOTasks convertEntityToDTOwithGroups(EntTasks task) {
        return new DTOTasks(
                task.getId(),
                task.getName(),
                task.getDatestart(),
                task.getDateend(),
                task.getText(),
                new DTOUserUpdate(
                        task.getCreatedby().getId(),
                        task.getCreatedby().getLogin(),
                        task.getCreatedby().getFirstname(),
                        task.getCreatedby().getLastname(),
                        task.getCreatedby().getSurname()
                ),
                task.getTasksubjectId(),
                task.getGroups()
        );
    }
}
