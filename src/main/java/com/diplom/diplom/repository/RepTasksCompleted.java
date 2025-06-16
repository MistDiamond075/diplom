package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntTasks;
import com.diplom.diplom.entity.EntTasksCompleted;
import com.diplom.diplom.entity.EntUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RepTasksCompleted extends JpaRepository<EntTasksCompleted,Long> {
    List<EntTasksCompleted> findAllByTasksId(EntTasks tasksId);
    Optional<EntTasksCompleted> findByUserIdAndTasksId(EntUser userId, EntTasks tasksId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tc FROM EntTasksCompleted tc WHERE tc.id=:id")
    Optional<EntTasksCompleted> findByIdWithLock(Long id);
}
