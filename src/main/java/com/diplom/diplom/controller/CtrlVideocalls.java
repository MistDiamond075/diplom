package com.diplom.diplom.controller;

import com.diplom.diplom.entity.EntVideocalls;
import com.diplom.diplom.entity.EntVideocallsHasUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.DataProcessingException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.exception.JanusAPIException;
import com.diplom.diplom.service.videocalls.ServiceVideocalls;
import com.diplom.diplom.service.videocalls.ServiceVideocallsAsync;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class CtrlVideocalls {
    private final ServiceVideocalls srvVideocalls;
    private final ServiceVideocallsAsync srvVideocallsAsync;

    public CtrlVideocalls(ServiceVideocalls srvVideocalls, ServiceVideocallsAsync srvVideocallsAsync) {
        this.srvVideocalls = srvVideocalls;
        this.srvVideocallsAsync = srvVideocallsAsync;
    }

    @GetMapping("/videocall/{id}/user/getData")
    public @ResponseBody EntVideocallsHasUser getUserData(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvVideocalls.getVideocallsHasUserByUserDetailsAndVideocallId(userDetails,id);
    }

    @GetMapping("/videocall/{id}/join")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> joinVideocall(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws URISyntaxException, ExecutionException, InterruptedException, AccessException, EntityException, JanusAPIException {
        return srvVideocallsAsync.joinVideocalls(id,userDetails);
    }

    @PostMapping("/videocall/{confId}/add")
    public @ResponseBody EntVideocalls addVideocall(@PathVariable Long confId, @AuthenticationPrincipal DiplomUserDetails userDetails) throws URISyntaxException, ExecutionException, InterruptedException, EntityException, JanusAPIException {
        return srvVideocalls.addVideocall(confId);
    }

    @PostMapping("/videocall/{id}/leave")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> leaveVideocall(@PathVariable Long id, @RequestParam(value = "reason") ServiceVideocalls.LeaveReasons reason, @AuthenticationPrincipal DiplomUserDetails userDetails) throws ExecutionException, InterruptedException, AccessException, EntityException, JanusAPIException {
        return srvVideocallsAsync.leaveVideocall(id,reason,userDetails);
    }

    @PostMapping("/videocall/{id}/user/update")
    public @ResponseBody CompletableFuture<ResponseEntity<?>> updateVideocallByAction(
            @PathVariable Long id,
            @RequestParam(value = "self") boolean self,
            @RequestParam(value = "userUpdatedId",required = false) Long userId,
            @RequestParam(value = "action") ServiceVideocalls.UpdateActions action,
            @RequestParam(value = "state",required = false) EntVideocallsHasUser.defaultStates state,
            @AuthenticationPrincipal DiplomUserDetails userDetails
    ) throws AccessException, EntityException, DataProcessingException {
        return self ? srvVideocallsAsync.updateUserSelfByAction(id,action,state,userDetails) : srvVideocallsAsync.updateUserOtherByAction(id,userId,action,state,userDetails);
    }
}
