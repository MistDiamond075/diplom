package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntVideocallChat;
import com.diplom.diplom.entity.EntVideocalls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepVideocallChat extends JpaRepository<EntVideocallChat, Long> {
    List<EntVideocallChat> findAllByVideocalluserId_VideocallsId(EntVideocalls id);
}
