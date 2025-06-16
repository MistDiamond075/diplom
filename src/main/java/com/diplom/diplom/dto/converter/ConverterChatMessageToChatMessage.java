package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOChatMessage;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.entity.EntChatMessage;

public class ConverterChatMessageToChatMessage {
    public static DTOChatMessage convertEntityToDTO(EntChatMessage msg) {
        return new DTOChatMessage(
                msg.getId(),
                msg.getText(),
                msg.getDate(),
                msg.getChatId().getId(),
                new DTOUserUpdate(
                        msg.getUser().getId(),
                        msg.getUser().getLogin(),
                        msg.getUser().getFirstname(),
                        msg.getUser().getLastname(),
                        msg.getUser().getSurname()
                ),
                msg.getReplyTo()!=null ? msg.getReplyTo().getId(): null
        );
    }
}
