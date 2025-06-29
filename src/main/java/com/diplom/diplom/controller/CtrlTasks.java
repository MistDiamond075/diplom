package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOTasks;
import com.diplom.diplom.entity.EntTasks;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.tasks.ServiceTasks;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class CtrlTasks {
    private final ServiceTasks srvTasks;

    public CtrlTasks(ServiceTasks srvTasks) {
        this.srvTasks = srvTasks;
    }

    @GetMapping("/getTasks")
    public @ResponseBody List<DTOTasks> getTasks(){
        return srvTasks.getTasks();
    }

    @PostMapping(value = "/tasks/addTask",consumes = {"multipart/form-data"})
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntTasks addTask(@RequestPart(value="taskdata") EntTasks task, @RequestPart(value="groups") String groups, @RequestParam(value="subjectname") String subjectname, @RequestParam(value="user_id") Long user_id, @RequestPart(value = "files",required = false)MultipartFile[] files) throws EntityException {
        return srvTasks.addTask(task,groups,subjectname,user_id,files);
    }

    @DeleteMapping("/tasks/{id}/deleteTask")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntTasks deleteTask(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvTasks.deleteTaskById(id,userDetails);
    }

    @PatchMapping("/tasks/update/{id}")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntTasks updTask(@RequestPart(value="taskdata") EntTasks task, @PathVariable Long id, @RequestParam(value="subjectname",required = false) String subjectname, @RequestPart(value="groups",required = false) String groups, @RequestPart(value = "files",required = false)MultipartFile[] files, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvTasks.updateTask(task,id,subjectname,groups,files,userDetails);
    }
}
