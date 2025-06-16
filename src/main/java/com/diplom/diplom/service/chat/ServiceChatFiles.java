package com.diplom.diplom.service.chat;

import com.diplom.diplom.repository.RepChatFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceChatFiles {
    private final RepChatFiles rChatFiles;

    @Autowired
    public ServiceChatFiles(RepChatFiles rChatFiles) {
        this.rChatFiles = rChatFiles;
    }
}
