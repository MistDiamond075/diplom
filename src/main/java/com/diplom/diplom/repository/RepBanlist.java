package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntBanlist;
import com.diplom.diplom.entity.EntUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepBanlist extends JpaRepository<EntBanlist, Long> {
    Optional<EntBanlist> findByBannedBy(EntUser bannedBy);

    Optional<EntBanlist> findByUserId(EntUser userId);

    boolean existsByUserId(EntUser userId);
}
