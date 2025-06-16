package com.diplom.diplom.service;

import com.diplom.diplom.entity.EntRole;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.repository.RepRole;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceRole {
    private final RepRole rRole;

    @Autowired
    public ServiceRole(RepRole rRole) {
        this.rRole = rRole;
    }

    public List<EntRole> getRoles(){
        return (List<EntRole>) rRole.findAll();
    }

    public EntRole getRole(Integer id) throws EntityException {
        return rRole.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "role with id "+id+" not found",
                "Роль не найдена",
                EntRole.class
        ));
    }

    public EntRole addRole(EntRole role){
        rRole.save(role);
        return role;
    }

    @Transactional
    public EntRole delRoleById(Integer id) throws EntityException {
        EntRole role=rRole.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "role not found when deleting by id "+id,
                "Роль не найдена",
                EntRole.class
        ));
        rRole.delete(role);
        return role;
    }

    @Transactional
    public EntRole updRole(Integer id, EntRole newrole) throws EntityException {
        EntRole role=rRole.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "role not found when deleting by id "+id,
                "Роль не найдена",
                EntRole.class
        ));
        role.setName(newrole.getName());
        role.setPower(newrole.getPower());
        rRole.save(role);
        return role;
    }
}
