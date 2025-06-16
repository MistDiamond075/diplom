package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.entity.EntTasksfiles;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.tasks.ServiceTasksfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CtrlTasksfiles {
    private final ServiceTasksfiles srvTasksfiles;

    @Autowired
    public CtrlTasksfiles(ServiceTasksfiles srvTasksfiles) {
        this.srvTasksfiles = srvTasksfiles;
    }

    @GetMapping("/admin/menu/getTaskfiles")
    @Secured("ROLE_ADMIN")
    public @ResponseBody List<DTOFile> getTaskfiles() {
        return srvTasksfiles.getTasksfiles();
    }

    @GetMapping("admin/menu/getTaskfile/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody DTOFile getTaskFile(@PathVariable Long id) throws EntityException {
        return srvTasksfiles.getTasksfile(id);
    }

    @GetMapping("/task/{taskId}/files")
    public ResponseEntity<List<Map<String, String>>> getTaskFiles(@PathVariable Long taskId,@AuthenticationPrincipal UserDetails userDetails) throws EntityException {
        return srvTasksfiles.getTaskFilesForFrontend(taskId,userDetails);
    }

    @GetMapping("/task/{taskId}/file/{id}/view")
    public ResponseEntity<Resource> viewFile(@PathVariable Long id) throws EntityException {
        return srvTasksfiles.getTaskFileForView(id);
    }

    @DeleteMapping("/task/{taskId}/file/{id}/delete")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public ResponseEntity<EntTasksfiles> deleteTaskFileById(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return ResponseEntity.ok(srvTasksfiles.deleteByFileId(id,userDetails));
    }
}
