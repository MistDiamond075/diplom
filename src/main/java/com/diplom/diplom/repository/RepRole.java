package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntRole;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RepRole extends CrudRepository<EntRole,Integer> {
    Optional<EntRole> findByName(String name);
    List<EntRole> findByNameIn(List<String> names);
}
