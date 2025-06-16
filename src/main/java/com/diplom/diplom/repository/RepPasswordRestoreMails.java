package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntPasswordRestoreMails;
import com.diplom.diplom.entity.EntUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepPasswordRestoreMails extends CrudRepository<EntPasswordRestoreMails,Long> {
    Optional<EntPasswordRestoreMails> findByMailuserId(EntUser user);
    Optional<EntPasswordRestoreMails> findByMailuuid(String uuid);
}
