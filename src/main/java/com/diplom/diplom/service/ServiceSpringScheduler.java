package com.diplom.diplom.service;

import com.diplom.diplom.entity.EntVideocallsHasUser;
import com.diplom.diplom.repository.RepVideocallsHasUser;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
public class ServiceSpringScheduler {
    private final RepVideocallsHasUser rVideocallsHasUser;

    public ServiceSpringScheduler(RepVideocallsHasUser rVideocallsHasUser) {
        this.rVideocallsHasUser = rVideocallsHasUser;
    }

    @PreDestroy
    public void destroy() {
        List<EntVideocallsHasUser> participants=new ArrayList<>();
        rVideocallsHasUser.findAll().forEach(participant ->{
            participant.setConnected(false);
            participants.add(participant);
        });
        rVideocallsHasUser.saveAll(participants);
    }
}
