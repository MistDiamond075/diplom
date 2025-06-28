package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.dto.DTOUserCss;
import com.diplom.diplom.dto.DTOUserSettings;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.entity.EntUserfiles;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.DataProcessingException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.user.ServiceUser;
import com.diplom.diplom.service.user.ServiceUserfiles;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class CtrlUserfiles {
    private final ServiceUserfiles srvUserfiles;
    private final ServiceUser srvUser;

    public CtrlUserfiles(ServiceUserfiles srvUserfiles, ServiceUser srvUser) {
        this.srvUserfiles = srvUserfiles;
        this.srvUser = srvUser;
    }

    @GetMapping("/admin/menu/getUserFiles")
    public List<DTOFile> getAvatars(){
        return srvUserfiles.getUserAvatars();
    }

    @GetMapping("/admin/menu/getUserFile/{id}")
    public DTOFile getUserAvatar(@PathVariable Long id) throws EntityException {
        return srvUserfiles.getUserAvatarById(id);
    }

    @GetMapping("/useravatar/{userid}")
    public ResponseEntity<Resource> getUserAvatarByUserId(@PathVariable Long userid) throws EntityException {
        return srvUserfiles.getUserAvatar(userid);
    }

    @GetMapping("/usercss")
    public ResponseEntity<Resource> getUserCssByUserId(HttpServletRequest request,@AuthenticationPrincipal DiplomUserDetails userDetails) throws EntityException {
        return srvUserfiles.getUserCss(request,userDetails);
    }

    @GetMapping("/usersettings")
    public DTOUserSettings getUserSettings(@AuthenticationPrincipal DiplomUserDetails userDetails) throws EntityException, AccessException, DataProcessingException {
        return srvUserfiles.getUserSettings(userDetails);
    }

    @PostMapping("/addUserAvatar")
    public @ResponseBody EntUserfiles addUserAvatar(@RequestParam(value = "imgfile",required = false) MultipartFile file, @RequestParam(value = "userId") Long userId, @AuthenticationPrincipal DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntUser user=srvUser.getUserById(userId);
        return srvUserfiles.addUserAvatar(file,user,userDetails);
    }

    @PostMapping("/addUserCss")
    public void addUserCss(@RequestBody DTOUserCss css,@AuthenticationPrincipal UserDetails userDetails) throws EntityException, AccessException, IOException {
        srvUserfiles.addUserCss(css.getText(),userDetails);
    }

    @PostMapping("/addUserSettings")
    public DTOUserSettings addUserSettings(@RequestBody DTOUserSettings settings,@AuthenticationPrincipal DiplomUserDetails userDetails) throws EntityException, AccessException, IOException {
        return srvUserfiles.addUserSettings(settings,userDetails);
    }

    @DeleteMapping("/deleteUserAvatar/{id}")
    public @ResponseBody DTOFile deleteUserAvatar(@PathVariable Long id) throws EntityException {
        return srvUserfiles.deleteUserFile(id);
    }
}
