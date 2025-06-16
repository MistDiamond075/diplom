package com.diplom.diplom.service.user;

import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceUserData {
    private final RepTasks rTasks;
    private final RepConferences rConferences;
    private final RepUser rUser;

    public ServiceUserData(RepTasks rTasks, RepConferences rConferences, RepUser rUser) {
        this.rTasks = rTasks;
        this.rConferences = rConferences;
        this.rUser = rUser;
    }

    public ResponseEntity<Map<String, List<Object>>> getTasksAndVideocallsForUser(int year,int month,UserDetails userDetails) throws EntityException {
        EntUser user=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with login "+userDetails.getUsername()+" not found when getting tasks and videocalls data",
                "Пользователь не найден",
                EntUser.class
        ));
        LocalDateTime datestart=LocalDateTime.of(year,month,1,0,0);
        YearMonth dateendmonth=YearMonth.of(year,month);
        LocalDateTime dateend=dateendmonth.atEndOfMonth().atTime(23,59,59);
        List<Long> groupIds = user.getGroups().stream()
                .map(EntGroup::getId)
                .toList();
        List<EntTasks> tasks=rTasks.findTasksByGroupAndMonth(groupIds,datestart,dateend);
        List<EntConferences> conferences=rConferences.findTasksByMonth(datestart,dateend);
        Map<String, List<Object>> result = new HashMap<>();
        result.put("tasks", new ArrayList<>(tasks));
        result.put("conferences", new ArrayList<>(conferences));
        return ResponseEntity.ok(result);
    }
}
