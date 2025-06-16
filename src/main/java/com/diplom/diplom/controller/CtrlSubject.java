package com.diplom.diplom.controller;

import com.diplom.diplom.entity.EntSubject;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.ServiceSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CtrlSubject {
    private final ServiceSubject srvSubject;

    @Autowired
    public CtrlSubject(ServiceSubject srvSubject) {
        this.srvSubject = srvSubject;
    }
    
    @GetMapping({"/admin/menu/getSubjects","/getSubjects"})
    public @ResponseBody List<EntSubject> getSubjects(){
        return srvSubject.getSubjects();
    }

    @GetMapping({"/admin/menu/getSubject/{id}","/getSubject/{id}"})
    public @ResponseBody EntSubject getSubject(@PathVariable Integer id) throws EntityException {
        return srvSubject.getSubject(id);
    }

    @PostMapping("/admin/menu/addSubject")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntSubject addSubject(@RequestBody EntSubject subject){
        return srvSubject.addSubject(subject);
    }

    @DeleteMapping("/admin/menu/deleteSubject/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntSubject delSubjectById(@PathVariable Integer id) throws EntityException {
        return srvSubject.delSubjectById(id);
    }

    @PatchMapping("/admin/menu/updateSubject/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntSubject updSubject(@PathVariable Integer id, @RequestBody EntSubject subject) throws EntityException {
        return srvSubject.updSubject(id,subject);
    }
}
