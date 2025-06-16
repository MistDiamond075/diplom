package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntTasks;
import com.diplom.diplom.entity.EntTasksfiles;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RepTasksFiles extends CrudRepository<EntTasksfiles,Long> {
    List<EntTasksfiles> findAllByTaskId(EntTasks task);
}
