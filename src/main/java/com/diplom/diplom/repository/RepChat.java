package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntChat;
import com.diplom.diplom.entity.EntGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepChat extends JpaRepository<EntChat, Long> {
    List<EntChat> findAllByGroupId(EntGroup groupId);
}
