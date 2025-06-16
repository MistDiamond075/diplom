package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntConferences;
import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepConferences extends JpaRepository<EntConferences,Long> {
    List<EntConferences> findAllByGroupIdIn(List<EntGroup> groupId);
    List<EntConferences> findAllByCreatedby(EntUser user);

    @Query("SELECT t FROM EntConferences t WHERE t.datestart BETWEEN :start AND :end")
    List<EntConferences> findTasksByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM EntConferences c WHERE c.id = :id")
    Optional<EntConferences> findByIdwithLock(@Param("id") Long id);
}
