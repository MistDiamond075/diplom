package com.diplom.diplom.dto;

import com.diplom.diplom.entity.EntJournal;

public class DTOJournal {
    private EntJournal journal;
    private String groups;

    public DTOJournal(EntJournal journal, String groups) {
        this.journal = journal;
        this.groups = groups;
    }

    public DTOJournal() {
    }

    public EntJournal getJournal() {
        return journal;
    }

    public void setJournal(EntJournal journal) {
        this.journal = journal;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }
}
