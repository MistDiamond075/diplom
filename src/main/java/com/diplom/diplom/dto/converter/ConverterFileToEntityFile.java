package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.DataProcessingException;
import com.diplom.diplom.misc.utils.Parser;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;

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

    public static DTOFile convertChatFileToDTOFile(EntChatfiles chatfile){
        try {
            return new DTOFile(
                    chatfile.getId(),
                    chatfile.getPath(),
                    "/chats/"+chatfile.getMessageId().getChatId().getId()+"/message/"+chatfile.getMessageId().getId()+"/file/"+chatfile.getId()+"/view",
                    chatfile.getMessageId().getId(),
                    Parser.parseFileContentType(Path.of(chatfile.getPath()))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
