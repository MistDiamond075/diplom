package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntConferences;
import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.entity.EntVideocalls;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepVideocalls extends JpaRepository<EntVideocalls,Long> {
    List<EntVideocalls> findAllByConferencesId_Createdby(EntUser conferencesIdCreatedby);
    Optional<EntVideocalls> findByConferencesId(EntConferences conferencesId);
    List<EntVideocalls> findAllByConferencesId_GroupIdIn(List<EntGroup> conferencesId_groupId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM EntVideocalls v WHERE v.id = :id")
    Optional<EntVideocalls> findByIdwithLock(@Param("id") Long id);
}
