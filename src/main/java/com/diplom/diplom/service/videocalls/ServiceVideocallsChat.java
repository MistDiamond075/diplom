package com.diplom.diplom.service.videocalls;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOMessageVideocall;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ServiceVideocallsChat {
    private final RepVideocalls rVideocalls;
    private final RepVideocallChat rVideocallChat;
    private final RepUser rUser;
    private final RepVideocallsHasUser rVideocallsHasUser;
    private final RepVideocallsHasUserProperties rVideocallsHasUserProperties;

    public ServiceVideocallsChat(RepVideocalls rVideocalls, RepVideocallChat rVideocallChat, RepUser rUser, RepVideocallsHasUser rVideocallsHasUser, RepVideocallsHasUserProperties rVideocallsHasUserProperties) {
        this.rVideocalls = rVideocalls;
        this.rVideocallChat = rVideocallChat;
        this.rUser = rUser;
        this.rVideocallsHasUser = rVideocallsHasUser;
        this.rVideocallsHasUserProperties = rVideocallsHasUserProperties;
    }

    public List<EntVideocallChat> getMessages(EntVideocalls videocallId){
        EntVideocalls videocall=rVideocalls.findById(videocallId.getId()).orElseThrow(()->new RuntimeException("videocall with id "+videocallId+" not found"));
        return rVideocallChat.findAllByVideocalluserId_VideocallsId(videocall);
    }

    @Transactional
    public DTOMessageVideocall addMessage(Long videocallId, String message_text,Long replyTo, DiplomUserDetails userDetails) throws AccessException {
        EntUser user=rUser.findByLogin(Objects.requireNonNull(userDetails).getUsername()).orElseThrow(()->new RuntimeException("user with login "+userDetails.getUsername()+" not found"));
        EntVideocalls videocall=rVideocalls.findById(videocallId).orElseThrow(()->new RuntimeException("videocall with id "+videocallId+" not found"));
        EntVideocallsHasUser videocallHasUser=rVideocallsHasUser.findByVideocalluserId(user,videocall).orElseThrow(()->new RuntimeException("videocallhasuser with user id "+user.getId()+" and videocall id"+videocall.getId()+" not found"));
        EntVideocallsHasUserProperties properties=rVideocallsHasUserProperties.findByvideocallHasUserId(videocallHasUser).orElse(null);
        if(properties!=null && properties.isBanned()) {
            throw new AccessException(HttpStatus.FORBIDDEN,"user with id "+videocallHasUser.getVideocalluserId().getId()+ " trying to disconnect to videocall with id "+videocallHasUser.getVideocallsId().getId()
                    +" where he was banned in videocall","Ошибка доступа: вы были забанены в этом звонке",userDetails);
        }
        EntVideocallsHasUser replyToUser=null;
        if(message_text.contains("@")){
            replyToUser=rVideocallsHasUser.findByVideocalluserId_Id(replyTo).orElse(null);
        }
        EntVideocallChat message=new EntVideocallChat(null,message_text, LocalDateTime.now(),videocallHasUser,replyToUser);
        rVideocallChat.save(message);
        return new DTOMessageVideocall(
                message.getId(),
                message.getMessage(),
                message.getDate(),
                message.getVideocalluserId(),
                replyToUser!=null ? replyToUser.getVideocalluserId().getId() : null,
                replyToUser!=null ? replyToUser.getVideocalluserId().getLogin() : null
        );
    }
}
