package com.diplom.diplom.service.chat;

import com.diplom.diplom.dto.DTOChatMessage;
import com.diplom.diplom.dto.converter.ConverterChatMessageToChatMessage;
import com.diplom.diplom.dto.converter.ConverterFileToEntityFile;
import com.diplom.diplom.entity.EntChat;
import com.diplom.diplom.entity.EntChatMessage;
import com.diplom.diplom.entity.EntChatUser;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Parser;
import com.diplom.diplom.repository.RepChat;
import com.diplom.diplom.repository.RepChatMessage;
import com.diplom.diplom.repository.RepChatUser;
import com.diplom.diplom.repository.RepUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceChatMessage {
    private final RepChatMessage rChatMessage;
    private final RepChat rChat;
    private final RepUser rUser;
    private final RepChatUser rChatUser;

    @Autowired
    public ServiceChatMessage(RepChatMessage rChatMessage, RepChat rChat, RepUser rUser, RepChatUser rChatUser) {
        this.rChatMessage = rChatMessage;
        this.rChat = rChat;
        this.rUser = rUser;
        this.rChatUser = rChatUser;
    }

    public List<DTOChatMessage> getMessagesInChat(Long chatId,int page,UserDetails userDetails) throws EntityException, AccessException {
        if(userDetails==null){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user details was null",
                    "Ошибка проверки данных аккаунта",
                    null
            );
        }
        EntUser user=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntChat chat=rChat.findById(chatId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "chat with id "+chatId+" not found",
                "Чат не найден",
                EntChat.class
        ));
        EntChatUser chatUser=rChatUser.findByChatIdAndUserId(chat,user).orElseThrow(()->new EntityException(
                HttpStatus.FORBIDDEN,
                "user with id "+user.getId()+" isn't member of chat with id "+chat.getId(),
                "Нет доступа к этому чату",
                EntChatUser.class
        ));
        Pageable pageable = PageRequest.of(page, 40);
        List<EntChatMessage> messages= rChatMessage.findAllByChatId(chat,pageable).getContent();
        return messages.stream().map(ConverterChatMessageToChatMessage::convertEntityToDTO).collect(Collectors.toList());
    }

    @Transactional
    public DTOChatMessage addMessageToChat(Long chatId, Long replyId, EntChatMessage msg, UserDetails userDetails) throws EntityException, AccessException {
        if(userDetails==null){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user details was null",
                    "Ошибка проверки данных аккаунта",
                    null
            );
        }
        EntChat chat=rChat.findById(chatId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "chat with id "+chatId+" not found",
                "Чат не найден",
                EntChat.class
        ));
        EntUser user=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntChatUser chatUser=rChatUser.findByChatIdAndUserId(chat,user).orElseThrow(()->new EntityException(
                HttpStatus.FORBIDDEN,
                "user with id "+user.getId()+" isn't member of chat with id "+chat.getId(),
                "Нет доступа к этому чату",
                EntChatUser.class
        ));
        EntChatMessage replyTo=null;
        if(replyId!=null){
            replyTo=rChatMessage.findById(replyId).orElse(null);
        }
        String text= Parser.parseXssText(msg.getText());
        EntChatMessage message=new EntChatMessage(
                null,
                text,
                LocalDateTime.now(),
                chat,
                user,
                replyTo
        );
        rChatMessage.save(message);
        return ConverterChatMessageToChatMessage.convertEntityToDTO(message);
    }

    @Transactional
    public DTOChatMessage deleteMessageFromChat(Long messageId, UserDetails userDetails) throws EntityException, AccessException {
        if(userDetails==null){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user details was null",
                    "Ошибка проверки данных аккаунта",
                    null
            );
        }
        EntUser user=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntChatMessage message=rChatMessage.findById(messageId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "message with id "+messageId+" not found",
                "Сообщение не найдено",
                EntChatMessage.class
        ));
        if(!message.getUser().equals(user)){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user isn't message creator",
                    "Это не ваше сообщение",
                    null
            );
        }
        rChatMessage.delete(message);
        return ConverterChatMessageToChatMessage.convertEntityToDTO(message);
    }
}
