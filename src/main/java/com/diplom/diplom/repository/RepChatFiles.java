package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntChatMessage;
import com.diplom.diplom.entity.EntChatfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RepChatFiles extends JpaRepository<EntChatfiles,Long> {
    List<EntChatfiles> findAllByMessageId(EntChatMessage messageId);
    List<EntChatfiles> findAllByMessageIdIn(Collection<EntChatMessage> messageIds);
}
