package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntSubject;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RepSubject extends CrudRepository<EntSubject,Integer> {
    Optional<EntSubject> findByName(String name);

    @Modifying
    @Query("UPDATE EntSubject s SET s.name = :name WHERE s.id = :id")
    void updateSubjectName(@Param(value = "name") String name, @Param(value = "id") Integer id);
}
