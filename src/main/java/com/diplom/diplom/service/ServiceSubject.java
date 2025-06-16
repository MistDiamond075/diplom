package com.diplom.diplom.service;

import com.diplom.diplom.entity.EntSubject;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.repository.RepSubject;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceSubject {
    private final RepSubject rSubject;

    @Autowired
    public ServiceSubject(RepSubject rSubject) {
        this.rSubject = rSubject;
    }

    public List<EntSubject> getSubjects(){
        return (List<EntSubject>) rSubject.findAll();
    }

    public EntSubject getSubject(Integer id) throws EntityException {
        return rSubject.findById(id).orElseThrow(()->new EntityException(
            HttpStatus.NOT_FOUND,
            "subject with id "+id+" not found",
            "Предмет не найден",
            EntSubject.class
        ));
    }

    public EntSubject addSubject(EntSubject subject){
        rSubject.save(subject);
        return subject;
    }

    @Transactional
    public EntSubject delSubjectById(Integer id) throws EntityException {
        EntSubject subject=rSubject.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "subject not found when deleting by id "+id,
                "Предмет не найден",
                EntSubject.class
        ));
        rSubject.delete(subject);
        return subject;
    }

    @Transactional
    public EntSubject updSubject(Integer id, EntSubject newsubject) throws EntityException {
        EntSubject subject=rSubject.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "subject not found when deleting by id "+id,
                "Предмет не найден",
                EntSubject.class
        ));
        rSubject.updateSubjectName(newsubject.getName(), id);
        return subject;
    }
}
