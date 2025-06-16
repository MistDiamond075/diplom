package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntVideocallsHasUser;
import com.diplom.diplom.entity.EntVideocallsHasUserProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepVideocallsHasUserProperties extends JpaRepository<EntVideocallsHasUserProperties,Long> {
    Optional<EntVideocallsHasUserProperties> findByvideocallHasUserId(EntVideocallsHasUser videocallsHasUserId);
}
