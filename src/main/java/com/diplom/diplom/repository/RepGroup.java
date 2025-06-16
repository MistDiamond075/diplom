package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepGroup extends CrudRepository<EntGroup,Long> {
    Optional<EntGroup> findByName(String name);

    List<EntGroup> findAllByNameIn(String[] names);
}
