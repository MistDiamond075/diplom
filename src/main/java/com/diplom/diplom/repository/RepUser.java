package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntRole;
import com.diplom.diplom.entity.EntUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepUser extends JpaRepository<EntUser,Long> {
    Optional<EntUser> findByLogin(String login);
    Optional<EntUser> findByFirstname(String name);
    Optional<EntUser> findByStudentcard(String cardnumber);
    Page<EntUser> findAll(Pageable pageable);
    List<EntUser> findAllByLoginIn(List<String> logins);
    List<EntUser> findAllByGroups(EntGroup group);
    List<EntUser> findAllByRolesIn(List<EntRole> roles);
}
