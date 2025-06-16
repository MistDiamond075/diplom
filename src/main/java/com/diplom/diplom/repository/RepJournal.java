package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntJournal;
import com.diplom.diplom.entity.EntTasksCompleted;
import com.diplom.diplom.entity.EntUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepJournal extends CrudRepository<EntJournal,Long> {
    List<EntJournal> findAllByJournaluserId(EntUser user);
    Optional<EntJournal> findByJournaltasksCompletedId(EntTasksCompleted journaltasksCompletedId);
}
