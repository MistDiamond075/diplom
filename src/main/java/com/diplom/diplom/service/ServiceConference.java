package com.diplom.diplom.service;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntConferences;
import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntSubject;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.repository.RepConferences;
import com.diplom.diplom.repository.RepGroup;
import com.diplom.diplom.repository.RepSubject;
import com.diplom.diplom.repository.RepUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServiceConference {
    private final RepConferences rConferences;
    private final RepUser rUser;
    private final RepSubject rSubject;
    private final RepGroup rGroup;

    @Autowired
    public ServiceConference(RepConferences rConferences, RepUser rUser, RepSubject rSubject, RepGroup rGroup) {
        this.rConferences = rConferences;
        this.rUser = rUser;
        this.rSubject = rSubject;
        this.rGroup = rGroup;
    }

    public List<EntConferences> getConferences(){
        return rConferences.findAll();
    }

    public EntConferences getConferenceById(Long id) throws EntityException {
        return rConferences.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "conference with id "+id+" not found",
                "Конференция не найдена",
                EntConferences.class
        ));
    }

    public List<EntConferences> getConferencesByUserGroup(DiplomUserDetails userDetails, EntUser user) throws AccessException {
        if(!Checker.checkUserIdentity(userDetails,user,rUser)){
            throw new AccessException(HttpStatus.UNAUTHORIZED,"user identity check failed","Ошибка проверки данных аккаунта",userDetails);
        }
        return rConferences.findAllByGroupIdIn(user.getGroups());
    }

    public List<EntConferences> getConferencesCreatedByUser(DiplomUserDetails userDetails, EntUser user) throws AccessException {
        if(!Checker.checkUserIdentity(userDetails,user,rUser)){
            throw new AccessException(HttpStatus.UNAUTHORIZED,"user identity check failed","Ошибка проверки данных аккаунта",userDetails);
        }
        return rConferences.findAllByCreatedby(user);
    }

    @Transactional
    public EntConferences addConference(EntConferences conference, String groups, String subjectname, UserDetails userDetails) throws EntityException {
        EntUser user=rUser.findByLogin(Objects.requireNonNull(userDetails).getUsername()).orElseThrow(()->
                new EntityException(
                        HttpStatus.NOT_FOUND,
                        "user with login "+ userDetails.getUsername() +" not found when adding conference",
                        "Пользователь не найден",
                        EntUser.class
                ));
        conference.setCreatedby(user);
        EntSubject subject=rSubject.findByName(subjectname).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "subject "+subjectname+" not found when adding task",
                "Предмет не найден",
                EntSubject.class
        ));
        conference.setSubjectId(subject);
        List<EntGroup> groupList = Arrays.stream(groups.split(","))
                .map(group -> {
                    try {
                        return rGroup.findByName(group)
                                .orElseThrow(() -> new EntityException(
                                        HttpStatus.NOT_FOUND,
                                        "group " + group + " not found when adding task",
                                        "Группы не найдены",
                                        EntGroup.class
                                ));
                    } catch (EntityException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        conference.setGroupId(groupList);
        rConferences.save(conference);
        return conference;
    }

    @Transactional
    public EntConferences updateConferenceById(EntConferences newconference, Long conferenceId, String groups, String subjectname, DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntConferences conference=rConferences.findById(conferenceId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "conference with id "+conferenceId+" not found when updating",
                "Конференция не найдена",
                EntConferences.class
        ));
        if(!Checker.checkUserIdentity(userDetails,conference.getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.UNAUTHORIZED,"user identity check failed","Ошибка проверки данных аккаунта",userDetails);
        }
        if(!conference.getSubjectId().getName().equals(Objects.requireNonNull(subjectname))){
            EntSubject subject=rSubject.findByName(subjectname).orElseThrow(()->new EntityException(
                    HttpStatus.NOT_FOUND,
                    "subject "+subjectname+" not found when updating conference",
                    "Предмет не найден",
                    EntSubject.class
            ));
            conference.setSubjectId(subject);
        }
        if(groups!=null) {
            List<String> newgroups = Arrays.stream(groups.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            List<String> oldgroups = conference.getGroupId().stream()
                    .map(EntGroup::getName)
                    .toList();
            if (!new HashSet<>(oldgroups).equals(new HashSet<>(newgroups))) {
                List<EntGroup> groupList = new ArrayList<>();
                for (String newgroup : newgroups) {
                    EntGroup group = rGroup.findByName(newgroup).orElseThrow(() -> new EntityException(
                            HttpStatus.NOT_FOUND,
                            "group " + newgroup + " not found when updating conference",
                            "Группа не найдена",
                            EntGroup.class
                    ));
                    groupList.add(group);
                }
                conference.setGroupId(groupList);
            }
        }
        conference.setDatestart(newconference.getDatestart());
        conference.setName(newconference.getName());
        conference.setRepeatable(newconference.getRepeatable());
        rConferences.save(conference);
        return conference;
    }

    @Transactional
    public EntConferences deleteConferenceById(Long id, DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntConferences conference=rConferences.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "conference not found when deleting by id "+id,
                "Конференция не найдена",
                EntConferences.class
        ));
        if(!Checker.checkUserIdentity(userDetails,conference.getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.UNAUTHORIZED,"user identity check failed","Ошибка проверки данных аккаунта",userDetails);
        }
        rConferences.delete(conference);
        return conference;
    }
}
