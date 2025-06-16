package com.diplom.diplom.controller;

import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.ServiceGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CtrlGroup {
    private final ServiceGroup srvGroup;

    @Autowired
    public CtrlGroup(ServiceGroup srvGroup) {
        this.srvGroup = srvGroup;
    }

    @GetMapping({"/admin/menu/getGroups","/getGroups"})
    public @ResponseBody List<EntGroup> getGroups() {
        return srvGroup.getGroups();
    }

    @GetMapping({"/admin/menu/getGroup/{id}","/getGroup/{id}"})
    public @ResponseBody EntGroup getGroup(@PathVariable Long id) throws EntityException {
        return srvGroup.getGroup(id);
    }

    @PostMapping("/admin/menu/addGroup")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntGroup addGroup(@RequestBody EntGroup group){
       return srvGroup.addGroup(group);
    }

    @DeleteMapping("/admin/menu/deleteGroup/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntGroup delGroupById(@PathVariable Long id) throws EntityException {
    return srvGroup.deleteGroupById(id);
    }

    @PatchMapping("/admin/menu/updateGroup/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntGroup updGroup(@PathVariable Long id, @RequestBody EntGroup group) throws EntityException {
      return  srvGroup.updateGroup(id,group);
    }
}
