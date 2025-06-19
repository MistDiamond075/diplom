package com.diplom.diplom.controller;

import com.diplom.diplom.entity.EntTasksCompleted;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.tasks.ServiceTasksCompleted;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class CtrlTasksCompleted {
    private final ServiceTasksCompleted srvTasksCompleted;

    public CtrlTasksCompleted(ServiceTasksCompleted srvTasksCompleted) {
        this.srvTasksCompleted = srvTasksCompleted;
    }

    @GetMapping("/getTasksCompleted")
    public @ResponseBody List<EntTasksCompleted> getTasksCompleted(){
        return srvTasksCompleted.getTasksCompleted();
    }

    @GetMapping("/task/{taskId}/getTaskCompletedByUserId")
    public @ResponseBody EntTasksCompleted getTaskCompletedByUseridAndTaskid(@RequestParam(value = "userId") Long userId, @PathVariable Long taskId){
        return srvTasksCompleted.getCompletedTaskByUserIdAndTaskId(userId,taskId);
    }

    @PostMapping("/task/{task_id}/addTaskCompleted")
    public @ResponseBody EntTasksCompleted addTasksCompleted(@RequestPart(value="taskdata") EntTasksCompleted task, @RequestParam(value = "user_id") Long user_id, @PathVariable Long task_id, @RequestPart(value="files",required = false) MultipartFile[] files, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvTasksCompleted.addTask(task,task_id,user_id,files,userDetails);
    }

    @DeleteMapping("/delTasksCompletedById")
    public @ResponseBody EntTasksCompleted delTasksCompletedById(Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvTasksCompleted.delTaskById(id,userDetails);
    }

    @PatchMapping("/task/{task_id}/updateTaskCompleted")
    public @ResponseBody EntTasksCompleted updTasksCompleted(@PathVariable Long task_id,@RequestPart(value="taskdata") EntTasksCompleted task,@RequestPart(value="files",required = false) MultipartFile[] files,  @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        task.setId(task_id);
        return srvTasksCompleted.updateTaskByUser(task,files,userDetails);
    }

    @PatchMapping("/task/{task_id}/checkTaskCompleted")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntTasksCompleted updateTaskCompletedByCheck(@RequestBody EntTasksCompleted task, @RequestParam(value = "taskId",required = false) Long task_id, @RequestParam(value = "userId",required = false) Long user_id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        srvTasksCompleted.updateTaskByCheck(task,userDetails);
        return task;
    }
}
