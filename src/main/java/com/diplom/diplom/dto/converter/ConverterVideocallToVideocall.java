package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOVideocall;
import com.diplom.diplom.entity.EntVideocalls;

public class ConverterVideocallToVideocall {
    public static DTOVideocall convertEntityToDTO(EntVideocalls videocall) {
        return new DTOVideocall(
                videocall.getId(),
                videocall.getRoomId(),
                videocall.getParticipants(),
                ConverterConferenceToConference.convertEntityToDTO(videocall.getConferencesId())
        );
    }
}
