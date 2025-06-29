package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOConferences;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.entity.EntConferences;

public class ConverterConferenceToConference {
    public static DTOConferences convertEntityToDTOwithGroups(EntConferences conference) {
        return new DTOConferences(
                conference.getId(),
                conference.getName(),
                conference.getDatestart(),
                conference.getDateend(),
                conference.getRepeatable(),
                conference.getGroupId(),
                conference.getSubjectId(),
                new DTOUserUpdate(
                        conference.getCreatedby().getId(),
                        conference.getCreatedby().getLogin(),
                        conference.getCreatedby().getFirstname(),
                        conference.getCreatedby().getLastname(),
                        conference.getCreatedby().getSurname()
                )
        );
    }

    public static DTOConferences convertEntityToDTO(EntConferences conference) {
        return new DTOConferences(
                conference.getId(),
                conference.getName(),
                conference.getDatestart(),
                conference.getDateend(),
                conference.getRepeatable(),
                conference.getSubjectId(),
                new DTOUserUpdate(
                        conference.getCreatedby().getId(),
                        conference.getCreatedby().getLogin(),
                        conference.getCreatedby().getFirstname(),
                        conference.getCreatedby().getLastname(),
                        conference.getCreatedby().getSurname()
                )
        );
    }
}
