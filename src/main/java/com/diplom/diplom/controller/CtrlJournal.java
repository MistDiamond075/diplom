package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntJournal;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.service.ServiceJournal;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CtrlJournal {
    private final ServiceJournal srvJournal;

    public CtrlJournal(ServiceJournal srvJournal) {
        this.srvJournal = srvJournal;
    }

    @GetMapping(path = "/getJournalByUserName")
    public @ResponseBody List<EntJournal> getAllJournalForUserByName(@RequestParam String name) throws EntityException {
        return srvJournal.getAllJournalForUserByName(name);
    }

    @PostMapping(path = "/addJournalUser")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntJournal addJournalUser(@RequestParam(value = "journaluserId") Long journaluserId, @RequestParam(value = "journalsubjectId") Integer journalsubjectId, @RequestBody EntJournal journal) throws EntityException {
        return srvJournal.addJournalUser(journal,journalsubjectId,journaluserId);
    }

    @PostMapping(path = "/addJournalGroup")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody List<EntJournal> addJournalGroup(@RequestParam(value = "journaluserId") Long journaluserId, @RequestParam(value = "journalsubjectId") Integer journalsubjectId, @RequestBody EntJournal journal) throws EntityException {
        return srvJournal.addJournalGroup(journal,journalsubjectId,journaluserId);
    }

    @DeleteMapping(path = "/delJournalUser/{id}")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntJournal delJournalUser(@PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvJournal.delJournalUserById(id,userDetails);
    }

    @PatchMapping(path = "/updJournalUser/{id}")
    @Secured({"ROLE_ADMIN","ROLE_TEACHER"})
    public @ResponseBody EntJournal updJournalUser(@RequestParam(value = "journaluserId") Long journaluserId, @RequestParam(value = "journalsubjectId") Integer journalsubjectId, @RequestBody EntJournal journal, @PathVariable Long id, @AuthenticationPrincipal DiplomUserDetails userDetails) throws AccessException, EntityException {
        return srvJournal.updJournalUser(journal,journalsubjectId,journaluserId,id,userDetails);
    }
}
