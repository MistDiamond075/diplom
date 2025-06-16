package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntChat;
import com.diplom.diplom.entity.EntChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepChatMessage extends JpaRepository<EntChatMessage, Long> {
    Page<EntChatMessage> findAllByChatId(EntChat chatId, Pageable pageable);
}
