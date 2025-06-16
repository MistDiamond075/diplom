package com.diplom.diplom.misc.utils.factory;

import com.diplom.diplom.entity.EntChat;
import com.diplom.diplom.entity.EntChatUser;
import com.diplom.diplom.entity.EntUser;

public class FactoryChatUser {
    public static EntChatUser createDefault(EntChat chat, EntUser user) {
        return new EntChatUser(null,chat, user);
    }
}
