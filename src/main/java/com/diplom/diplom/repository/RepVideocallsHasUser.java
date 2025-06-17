package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.entity.EntVideocalls;
import com.diplom.diplom.entity.EntVideocallsHasUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepVideocallsHasUser extends CrudRepository<EntVideocallsHasUser, Long> {
    @Query("SELECT v FROM EntVideocallsHasUser v WHERE v.videocallsId = :call AND v.videocalluserId = :user")
    Optional<EntVideocallsHasUser> findByVideocalluserId(@Param("user") EntUser videocalluserId, @Param("call") EntVideocalls call);
    void deleteAllByVideocallsId(EntVideocalls videocallsId);
    List<EntVideocallsHasUser> findAllByVideocallsId(EntVideocalls videocall);

    List<EntVideocallsHasUser> findAllByVideocalluserId(EntUser videocalluserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM EntVideocallsHasUser h WHERE h.videocalluserId = :user AND h.videocallsId = :call")
    Optional<EntVideocallsHasUser> findByVideocalluserIdwithLock(@Param("user") EntUser user, @Param("call") EntVideocalls videocall);

    Optional<EntVideocallsHasUser> findByVideocalluserId_Id(Long id);

    @Modifying
    @Query("UPDATE EntVideocallsHasUser v SET v.connected = :connected WHERE v.videocalluserId.id = :userId")
    void updateConnectionStatus(@Param("userId") Long userId, @Param("connected") boolean connected);

}
