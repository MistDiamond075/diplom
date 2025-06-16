package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntChat;
import com.diplom.diplom.entity.EntChatUser;
import com.diplom.diplom.entity.EntUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepChatUser extends JpaRepository<EntChatUser, Long> {
    Page<EntChatUser> findAllByChatId(EntChat entChat, Pageable pageable);

    Page<EntChatUser> findAllByUserId(EntUser userId, Pageable pageable);
    Optional<EntChatUser> findByChatIdAndUserId(EntChat chatId, EntUser userId);
}
