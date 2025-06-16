package com.diplom.diplom.service;

import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.repository.RepGroup;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceGroup {
    private final RepGroup rGroup;

    @Autowired
    public ServiceGroup(RepGroup rGroup) {
        this.rGroup = rGroup;
    }

    public List<EntGroup> getGroups(){
        return (List<EntGroup>) rGroup.findAll();
    }

    public EntGroup getGroup(Long id) throws EntityException {
        return rGroup.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "group with id "+id+" not found",
                "Группа не найдена",
                EntGroup.class
        ));
    }

    public List<EntGroup> getGroupsFromStudentsList(List<EntUser> students){
        return students.stream()
                .flatMap(user -> user.getGroups().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public EntGroup addGroup(EntGroup group){
        rGroup.save(group);
        return group;
    }

    @Transactional
    public EntGroup deleteGroupById(Long id) throws EntityException {
        EntGroup group=rGroup.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "group not found when deleting by id "+id,
                "Группа не найдена",
                EntGroup.class
        ));
        rGroup.delete(group);
        return group;
    }

    @Transactional
    public EntGroup updateGroup(Long id, EntGroup newgroup) throws EntityException {
        EntGroup group=rGroup.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "group not found when deleting by id "+id,
                "Группа не найдена",
                EntGroup.class
        ));
        group.setPrivateGroup(newgroup.getPrivateGroup());
        group.setName(newgroup.getName());
        rGroup.save(group);
        return group;
    }
}
