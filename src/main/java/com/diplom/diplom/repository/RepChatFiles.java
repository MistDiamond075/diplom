package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntChatfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepChatFiles extends JpaRepository<EntChatfiles,Long> {
}
