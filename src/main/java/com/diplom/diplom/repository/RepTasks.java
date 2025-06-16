package com.diplom.diplom.repository;

import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntTasks;
import com.diplom.diplom.entity.EntUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepTasks extends JpaRepository<EntTasks,Long> {
    List<EntTasks> findAllByGroupsIn(List<EntGroup> group);
    List<EntTasks> findAllByCreatedby(EntUser user);
    @Query("SELECT t FROM EntTasks t JOIN t.createdby u JOIN u.groups g WHERE g.id IN :groupIds AND t.dateend BETWEEN :start AND :end")
    List<EntTasks> findTasksByGroupAndMonth(@Param("groupIds") List<Long> groupIds, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
