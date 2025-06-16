package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.entity.*;

public class ConverterFileToEntityFile {
    public static DTOFile convertTaskFileToDTOFile(EntTasksfiles task) {
        return new DTOFile(
                task.getId(),
                task.getPath(),
                "/task/"+task.getTaskId().getId()+"/file/"+task.getId()+"/view",
                task.getTaskId().getId()
        );
    }

    public static DTOFile convertCompletedTaskFileToDTOFile(EntCompletedtasksfiles task) {
        return new DTOFile(
                task.getId(),
                task.getPath(),
                "/task/"+task.getTasksCompletedId().getId()+"/completedTask/file/"+task.getId()+"/view",
                task.getTasksCompletedId().getId()
        );
    }

    public static DTOFile convertUserFileToDTOFile(EntUserfiles userfile){
        return new DTOFile(
                userfile.getId(),
                userfile.getPath(),
                "/useravatar/"+userfile.getFilesuserId().getId(),
                userfile.getFilesuserId().getId()
        );
    }

    public static EntTasksfiles convertDTOFileToTaskFile(DTOFile file, EntTasks task) {
        return new EntTasksfiles(
           file.getId(),
          task,
          file.getPath()
        );
    }
}
