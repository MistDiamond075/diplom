package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntConferences;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.ServiceConference;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CtrlConference {
    private final ServiceConference srvConference;

    public CtrlConference(ServiceConference srvConference) {
        this.srvConference = srvConference;
    }

    @GetMapping("/getConferences")
    public @ResponseBody List<EntConferences> getConferences(){
        return srvConference.getConferences();
    }

    @PostMapping("/conference/create/addConference")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntConferences addConference(@RequestPart(value="conferencedata") EntConferences conference, @RequestPart(value="groups") String groups, @RequestParam(value="subjectname") String subjectname, @AuthenticationPrincipal UserDetails userDetails) throws EntityException {
        return srvConference.addConference(conference,groups,subjectname,userDetails);
    }

    @PatchMapping("/conference/{id}/update/updateConference")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntConferences updateConference(@RequestPart(value="conferencedata") EntConferences conference, @PathVariable Long id, @RequestPart(value="groups") String groups, @RequestParam(value="subjectname") String subjectname, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvConference.updateConferenceById(conference,id,groups,subjectname,userDetails);
    }

    @DeleteMapping("/conferences/{id}/deleteConference")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntConferences deleteConferenceById(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvConference.deleteConferenceById(id,userDetails);
    }
}
