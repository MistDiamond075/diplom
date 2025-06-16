package com.diplom.diplom.controller;

import com.diplom.diplom.entity.EntBanlist;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.ServiceBanlist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/menu")
public class CtrlBanlist {
    private final ServiceBanlist srvBanlist;

    public CtrlBanlist(ServiceBanlist srvBanlist) {
        this.srvBanlist = srvBanlist;
    }

    @GetMapping("/getBanlist")
    public @ResponseBody List<EntBanlist> getBanlist() {
        return srvBanlist.getBanlist();
    }

    @GetMapping("/getBan/{id}")
    public @ResponseBody EntBanlist getBan(@PathVariable Long id) throws EntityException {
        return srvBanlist.getBan(id);
    }

    @PostMapping("/addBan/{userId}")
    public @ResponseBody EntBanlist addBan(@RequestBody EntBanlist banlist, @PathVariable Long userId, HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) throws EntityException, AccessException {
        return srvBanlist.addBan(banlist,userId,userDetails);
    }

    @DeleteMapping("/deleteBan/{id}")
    public @ResponseBody EntBanlist deleteBan(@PathVariable Long id) throws EntityException {
        return srvBanlist.deleteBan(id);
    }

    @PatchMapping("/updateBan/{id}")
    public @ResponseBody EntBanlist updateBan(@PathVariable Long id, @RequestBody EntBanlist banlist) throws EntityException {
        return srvBanlist.updateBan(banlist, id);
    }
}
