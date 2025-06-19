package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.user.ServiceUser;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CtrlUser {
    private final ServiceUser srvUser;

    public CtrlUser(ServiceUser srvUser) {
        this.srvUser = srvUser;
    }

    @GetMapping("/getUsers")
    public @ResponseBody List<DTOUserUpdate> getUsers(@RequestParam(value="page") int page){
        return srvUser.getUsersAll(page);
    }

    @GetMapping(path = "/admin/menu/getUsersAll")
    @Secured("ROLE_ADMIN")
    public @ResponseBody List<DTOUserUpdate> getUsersAll(){
        return srvUser.getUsersDTO();
    }

    @GetMapping(path="/admin/menu/getUserById/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody DTOUserUpdate getUserById(@PathVariable Long id) throws EntityException {
        return srvUser.getFullDTOUserById(id);
    }

    @GetMapping(path = "/admin/menu/getUserByLogin")
    @Secured("ROLE_ADMIN")
    public @ResponseBody EntUser getUserByLogin(UserDetails userDetails) throws EntityException {
        return srvUser.getUserByLogin(userDetails);
    }

    @DeleteMapping(path="/admin/menu/delUser/{id}")
    @Secured("ROLE_ADMIN")
    public @ResponseBody DTOUserUpdate delUserById(@PathVariable Long id,@AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvUser.delUserById(id, userDetails);
    }

    @PatchMapping(path = "/updUser/{id}")
    public @ResponseBody EntUser updUser(@AuthenticationPrincipal DiplomUserDetails userDetails, @RequestBody DTOUserUpdate user, @PathVariable Long id, @RequestParam(value = "group_name",required = false) String[] group_name, @RequestParam(value="admin",required = false,defaultValue = "false") boolean admin) throws AccessException, EntityException {
        return srvUser.updUser(user,id,group_name,userDetails,admin);
    }
}
