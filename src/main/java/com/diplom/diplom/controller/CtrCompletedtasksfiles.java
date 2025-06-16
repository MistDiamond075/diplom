package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.entity.EntCompletedtasksfiles;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.tasks.ServiceCompletedtasksfiles;
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
public class CtrCompletedtasksfiles {
    private final ServiceCompletedtasksfiles srvCompletedtasksfiles;

    @Autowired
    public CtrCompletedtasksfiles(ServiceCompletedtasksfiles srvCompletedtasksfiles) {
        this.srvCompletedtasksfiles = srvCompletedtasksfiles;
    }

    @GetMapping("/admin/menu/getCompletedtaskFiles")
    @Secured("ROLE_ADMIN")
    public @ResponseBody List<DTOFile> getCompletedTaskFiles() {
        return srvCompletedtasksfiles.getCompletedtasksfiles();
    }

    @GetMapping("/admin/menu/getCompletedtaskFile/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody DTOFile getCompletedTaskFile(@PathVariable Long id) throws EntityException {
        return srvCompletedtasksfiles.getCompletedtaskFile(id);
    }

    @GetMapping("/task/{taskId}/completedTask/file/{id}")
    public ResponseEntity<List<Map<String, String>>> getCompletedTaskFiles(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return srvCompletedtasksfiles.getCompletedTaskFileForFrontend(id,userDetails);
    }

    @GetMapping("/task/{taskId}/completedTask/file/{id}/view")
    public ResponseEntity<Resource> viewFile(@PathVariable Long id) throws EntityException {
        return srvCompletedtasksfiles.getCompletedTaskFileForView(id);
    }

    @DeleteMapping("/task/{taskId}/completedTask/file/{id}/delete")
    public ResponseEntity<EntCompletedtasksfiles> deleteCompletedTaskFileById(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException {
        return ResponseEntity.ok(srvCompletedtasksfiles.deleteByFileId(id,userDetails));
    }

}
