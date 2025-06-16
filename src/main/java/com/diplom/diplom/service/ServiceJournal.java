package com.diplom.diplom.service;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.repository.RepGroup;
import com.diplom.diplom.repository.RepJournal;
import com.diplom.diplom.repository.RepSubject;
import com.diplom.diplom.repository.RepUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceJournal {
    private final RepJournal rJournal;
    private final RepUser rUser;
    private final RepSubject rSubject;
    private final RepGroup rGroup;

    @Autowired
    public ServiceJournal(RepJournal rJournal, RepUser rUser, RepSubject rSubject, RepGroup rGroup) {
        this.rJournal = rJournal;
        this.rUser = rUser;
        this.rSubject = rSubject;
        this.rGroup = rGroup;
    }

    public List<EntJournal> getAllJournal(){
        return (List<EntJournal>) rJournal.findAll();
    }

    public List<EntJournal> getAllJournalForUserByName(String name) throws EntityException {
        EntUser user=rUser.findByFirstname(name).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when getting journal by user name "+name,
                "Пользователь не найден",
                EntUser.class
        ));
        return rJournal.findAllByJournaluserId(user);
    }

    public EntJournal getJournalByCompletedTaskId(EntTasksCompleted completedTaskId) throws EntityException {
        return rJournal.findByJournaltasksCompletedId(completedTaskId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "journal not found by completedtask id "+ (completedTaskId!=null ? completedTaskId.getId() : "undefined"),
                "Запись журнала не найдена",
                EntJournal.class
        ));
    }

    @Transactional
    public EntJournal addJournalUser(EntJournal newjournal, Integer subject_id, Long user_id) throws EntityException {
        EntUser user=rUser.findById(user_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when adding journal to user "+user_id,
                "Пользователь не найден",
                EntUser.class
        ));
        EntSubject subject=rSubject.findById(subject_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "subject not found when adding journal with subject "+subject_id,
                "Предмет не найден",
                EntSubject.class
        ));
        newjournal.setJournaluserId(user);
        newjournal.setJournalsubjectId(subject);
        rJournal.save(newjournal);
        return newjournal;
    }

    @Transactional
    public List<EntJournal> addJournalGroup(EntJournal newjournal, Integer subject_id, Long group_id) throws EntityException {
        List<EntJournal> journalret=new ArrayList<>();
        EntGroup group=rGroup.findById(group_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "group not found when adding journal to group with id "+group_id,
                "Группа не найдена",
                EntGroup.class
                ));
        EntSubject subject=rSubject.findById(subject_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "subject not found when adding journal with subject "+subject_id,
                "Предмет не найден",
                EntSubject.class
        ));
        List<EntUser> userlist=rUser.findAllByGroups(group);
        if(userlist==null){
            throw new EntityException(
                    HttpStatus.NOT_FOUND,
                    "users in group not found when adding journal to group with id "+group_id,
                    "Пользователи в группе не найдены",
                    EntUser.class
            );
        }
        if(userlist.isEmpty()){
            return null;
        }
        for(EntUser user:userlist){
            EntJournal journal=new EntJournal(null,user,subject,newjournal.getDate(),newjournal.getGrade(),newjournal.getWaspresent());
            rJournal.save(journal);
            journalret.add(journal);
        }
        return journalret;
    }

    @Transactional
    public EntJournal delJournalUserById(Long id, DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntJournal journal=rJournal.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "journal not found when deleting by id "+id,
                "Запись в журнале не найдена",
                EntJournal.class
        ));
        if(!Checker.isUserHasAnyRole(userDetails.getUser(), new String[]{"ROLE_TEACHER", "ROLE_ADMIN"})){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        rJournal.delete(journal);
        return journal;
    }

    @Transactional
    public EntJournal updJournalUser(EntJournal newjournal, Integer subject_id, Long user_id, Long id, DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntJournal journal=rJournal.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "journal not found when updating by id "+id,
                "Запись в журнале не найдена",
                EntJournal.class
        ));
        EntUser user=rUser.findById(user_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when adding journal to user "+user_id,
                "Пользователь не найден",
                EntUser.class
        ));
        if(!Checker.isUserHasAnyRole(userDetails.getUser(), new String[]{"ROLE_TEACHER", "ROLE_ADMIN"})){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        EntSubject subject=rSubject.findById(subject_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "subject not found when adding journal with subject "+subject_id,
                "Предмет не найден",
                EntSubject.class
        ));
        if(newjournal.getGrade()!=null && newjournal.getGrade()>=0) {
            journal.setGrade(newjournal.getGrade());
        }else{
            journal.setGrade(null);
        }
        journal.setDate(newjournal.getDate());
        journal.setWaspresent(newjournal.getWaspresent());
        journal.setJournalsubjectId(subject);
        journal.setJournaluserId(user);
        rJournal.save(journal);
        return journal;
    }
}
