package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.dto.DTOVideocallsHasUser;
import com.diplom.diplom.entity.EntVideocallsHasUser;

public class ConverterVideocallsHasUserToVideocallsHasUser {
    public static DTOVideocallsHasUser convertEntityToDTO(EntVideocallsHasUser videocallsHasUser){
        return new DTOVideocallsHasUser(
                videocallsHasUser.getId(),
                ConverterVideocallToVideocall.convertEntityToDTO(videocallsHasUser.getVideocallsId()),
                new DTOUserUpdate(
                        videocallsHasUser.getVideocalluserId().getId(),
                        videocallsHasUser.getVideocalluserId().getLogin(),
                        videocallsHasUser.getVideocalluserId().getFirstname(),
                        videocallsHasUser.getVideocalluserId().getLastname(),
                        videocallsHasUser.getVideocalluserId().getSurname()
                ),
                videocallsHasUser.getMicrostate(),
                videocallsHasUser.getCamstate(),
                videocallsHasUser.getSoundstate(),
                videocallsHasUser.getDemostate(),
                videocallsHasUser.getSignalstate(),
                videocallsHasUser.getConnected()
        );
    }
}
