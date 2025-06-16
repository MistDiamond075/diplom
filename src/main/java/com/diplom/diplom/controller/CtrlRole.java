package com.diplom.diplom.controller;

import com.diplom.diplom.entity.EntRole;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.ServiceRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CtrlRole {
    private final ServiceRole srvRole;

    @Autowired
    public CtrlRole(ServiceRole srvRole) {
        this.srvRole = srvRole;
    }

    @GetMapping({"/admin/menu/getRoles","/getRoles"})
    public @ResponseBody List<EntRole> getRoles(){
        return srvRole.getRoles();
    }

    @GetMapping({"/admin/menu/getRole/{id}","/getRole/{id}"})
    public @ResponseBody EntRole getRole(@PathVariable Integer id) throws EntityException {
        return srvRole.getRole(id);
    }

    @PostMapping("/admin/menu/addRole")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntRole addRole(@RequestBody EntRole role){
        return srvRole.addRole(role);
    }

    @DeleteMapping("/admin/menu/deleteRole/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntRole delRoleById(@PathVariable Integer id) throws EntityException {
        return srvRole.delRoleById(id);
    }

    @PatchMapping("/admin/menu/updateRole/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntRole updRole(@PathVariable Integer id, @RequestBody EntRole role) throws EntityException {
        return srvRole.updRole(id,role);
    }
}
