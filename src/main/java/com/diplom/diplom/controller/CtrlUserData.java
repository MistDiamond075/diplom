package com.diplom.diplom.controller;

import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.user.ServiceUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CtrlUserData {
    private final ServiceUserData srvUserData;

    @Autowired
    public CtrlUserData(ServiceUserData srvUserData){
        this.srvUserData = srvUserData;
    }

    @GetMapping("/getUserData")
    public ResponseEntity<Map<String,List<Object>>> getTasksAndVideocallsForUser(@RequestParam(value = "year") int year,@RequestParam(value = "month") int month,@AuthenticationPrincipal UserDetails userDetails) throws EntityException {
        return srvUserData.getTasksAndVideocallsForUser(year,month,userDetails);
    }
}
