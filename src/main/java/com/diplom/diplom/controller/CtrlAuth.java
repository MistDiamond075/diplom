package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntPasswordRestoreMails;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.ServicePasswordRestoreMails;
import com.diplom.diplom.service.user.ServiceUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class CtrlAuth {
    private final ServiceUser srvUser;
    private final ServicePasswordRestoreMails srvPasswordRestoreMails;
    private final String datetime_pattern="yyyy-MM-dd HH:mm:ss";

    public CtrlAuth(ServiceUser srvUser, ServicePasswordRestoreMails srvPasswordRestoreMails) {
        this.srvUser = srvUser;
        this.srvPasswordRestoreMails = srvPasswordRestoreMails;
    }

    @PostMapping(path = "/registrationpage/regUser")
    public @ResponseBody EntUser addUser(@RequestBody EntUser user, @RequestParam(value="groupname") String[] groupname) throws EntityException {
        return srvUser.addUser(user,groupname);
    }

    @PostMapping(path = "/pwrestorepage/request")
    public void getRequestPwrestore(@RequestBody EntUser restoreduser, @RequestParam(value="email",required = false) String email) throws EntityException {
        EntUser user=srvUser.checkUserExistForPWRform(restoreduser);
        if(user!=null){
            if(email!=null){
                user.setEmail(email);
            }
            EntPasswordRestoreMails mail=srvPasswordRestoreMails.addMail(new EntPasswordRestoreMails(null,getCurrentDateTime(),generateUUID(),user));
            srvPasswordRestoreMails.sendMail(mail);
        }
    }

    @PostMapping(path="/pwrestorepage/pwupdate")
    public EntUser updUserPassword(@RequestParam(value="uuid") String uuid, @RequestParam(value="user_id") Long user_id, @RequestBody EntUser newuser) throws EntityException, AccessException {
        if(uuid==null || user_id==null){
            return null;
        }
        EntPasswordRestoreMails mail=srvPasswordRestoreMails.getMailByUUID(uuid);
        if(mail==null || mail.getMailuserId().getId()!=user_id){
            return null;
        }
        EntUser user=srvUser.getUserById(user_id);
        if(user==null){
            return null;
        }
        user.setPassword(newuser.getPassword());
        srvPasswordRestoreMails.delMail(mail);
        return srvUser.updUserByPwRestore(user,user_id);
    }

    private String getCurrentDateTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datetime_pattern);
        return now.format(formatter);
    }

    private String generateUUID(){
        return java.util.UUID.randomUUID().toString();
    }
}
