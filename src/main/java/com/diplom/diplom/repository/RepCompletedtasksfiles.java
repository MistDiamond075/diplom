package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntCompletedtasksfiles;
import com.diplom.diplom.entity.EntTasksCompleted;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface RepCompletedtasksfiles extends CrudRepository<EntCompletedtasksfiles,Long> {
    Collection<EntCompletedtasksfiles> findAllByTasksCompletedId(EntTasksCompleted tasksCompletedId);
}
