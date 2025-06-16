package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOChat;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.entity.EntChat;
import com.diplom.diplom.entity.EntChatUser;

import java.util.ArrayList;
import java.util.List;

public class ConverterChatToChat {
    public static DTOChat convertEntityToDTO(EntChat chat){
        List<EntChatUser> users=chat.getChatUsers();
        return new DTOChat(
                chat.getId(),
                chat.getName(),
                chat.getSubjectId()!=null ? chat.getSubjectId().getName() : null,
                chat.getGroupId()!=null ? chat.getGroupId().getName() : null,
                new ArrayList<>()
        );
    }
}
