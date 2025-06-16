package com.diplom.diplom.service.chat;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOChat;
import com.diplom.diplom.dto.DTOChatDisplay;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.dto.converter.ConverterChatToChat;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.factory.FactoryChatUser;
import com.diplom.diplom.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceChat {
    private final RepChat rChat;
    private final RepChatUser rChatUser;
    private final RepSubject rSubject;
    private final RepUser rUser;
    private final RepGroup rGroup;

    @Autowired
    public ServiceChat(RepChat rChat, RepChatUser rChatUser, RepSubject rSubject, RepUser rUser, RepGroup rGroup) {
        this.rChat = rChat;
        this.rChatUser = rChatUser;
        this.rSubject = rSubject;
        this.rUser = rUser;
        this.rGroup = rGroup;
    }

    public List<EntChat> getChatsAll(){
        return rChat.findAll();
    }

    public List<DTOChatDisplay> getChatsDTO(UserDetails userDetails,int page) throws AccessException, EntityException {
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
                "user with username "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        Pageable pageable = PageRequest.of(page, 20);
        List<EntChatUser> chats=rChatUser.findAllByUserId(user,pageable).getContent();
        List<DTOChatDisplay> dtoChats=new ArrayList<>();
        for(EntChatUser chatuser:chats){
            EntChat chat=chatuser.getChatId();
            List<DTOUserUpdate> users = chat.getChatUsers().stream().map(u -> new DTOUserUpdate(
                    u.getUserId().getId(),
                    u.getUserId().getLogin(),
                    u.getUserId().getFirstname(),
                    u.getUserId().getLastname(),
                    u.getUserId().getSurname(),
                    u.getUserId().getGroups().stream().map(EntGroup::getName).toList()
            )).toList();
            dtoChats.add(new DTOChatDisplay(
                    chat,
                    users
            ));
        }
        return dtoChats;
    }

    public DTOChat getUsersInChat(Long id, int page, DiplomUserDetails userDetails) throws EntityException, AccessException {
        if(userDetails==null){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user details was null",
                    "Ошибка проверки данных аккаунта",
                    null
            );
        }
        EntUser user=userDetails.getUser();
        EntChat chat= rChat.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "chat with id "+id+" not found",
                "Чат не найден",
                EntChat.class
        ));
        Pageable pageable = PageRequest.of(page, 20);
        Page<EntChatUser> chatusers=rChatUser.findAllByChatId(chat,pageable);
        List<EntUser> userList=chatusers.getContent().stream().map(EntChatUser::getUserId).toList();
        List<DTOUserUpdate> users=new ArrayList<>();
        for(EntUser user1:userList){
            users.add(new DTOUserUpdate(
                user1.getId(),
                user1.getLogin(),
                user1.getFirstname(),
                user1.getLastname(),
                user1.getSurname()
            ));
        }
        if(!userList.contains(user)){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user tried to delete chat, which doesn't belong to him",
                    "Вы не можете получить пользователей этого чата",
                    userDetails
            );
        }
        return new DTOChat(
                chat.getId(),
                chat.getName(),
                chat.getSubjectId()!=null ? chat.getSubjectId().getName() : null,
                chat.getGroupId()!=null ? chat.getGroupId().getName() : null,
                users
        );
    }

    public DTOChatDisplay getChat(Long id) throws EntityException {
        EntChat chat= rChat.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "chat with id "+id+" not found",
                "Чат не найден",
                EntChat.class
        ));
        return new DTOChatDisplay(
                chat,
                chat.getChatUsers().stream().map(u -> new DTOUserUpdate(
                        u.getUserId().getId(),
                        u.getUserId().getLogin(),
                        u.getUserId().getFirstname(),
                        u.getUserId().getLastname(),
                        u.getUserId().getSurname(),
                        u.getUserId().getGroups().stream().map(EntGroup::getName).toList()
                )).toList()
        );
    }

    @Transactional
    public DTOChat addChat(DTOChat newChat, UserDetails userDetails) throws EntityException, AccessException {
        EntSubject subject=rSubject.findByName(newChat.getSubjectName()).orElse(null);
        EntGroup group=rGroup.findByName(newChat.getGroupName()).orElse(null);
        if(userDetails==null){
            throw new AccessException(
               HttpStatus.UNAUTHORIZED,
               "user details was null",
               "Ошибка проверки данных аккаунта",
               null
            );
        }
        EntUser creator=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with username "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        Set<EntUser> members=new HashSet<>(rUser.findAllByLoginIn(Arrays.stream(newChat.getMembers()).toList()));
        members.add(creator);
        if(group!=null){
            members.addAll(group.getUsers_list());
        }
        EntChat chat=new EntChat(null, newChat.getName(), subject, creator, group);
        List<EntChatUser> chatUsers = members.stream()
                .map(user -> new EntChatUser(null, chat, user))
                .toList();
        chat.setChatUsers(chatUsers);
        rChat.save(chat);
        DTOChat retChat= ConverterChatToChat.convertEntityToDTO(chat);
        retChat.setMembersList(members.stream().map(user -> new DTOUserUpdate(
                user.getId(),
                user.getLogin(),
                user.getFirstname(),
                user.getLastname(),
                user.getSurname()
        )).toList());
        return retChat;
    }

    @Transactional
    public DTOChat deleteChat(Long id,DiplomUserDetails userDetails) throws EntityException, AccessException {
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
                "user with username "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntChat chat=rChat.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "chat with id "+id+" not found",
                "Чат не найден",
                EntChat.class
        ));
        if(!chat.getCreateBy().equals(user)){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user tried to delete chat, which doesn't belong to him",
                    "Вы не можете удалить этот чат",
                    userDetails
            );
        }
        rChat.delete(chat);
        return ConverterChatToChat.convertEntityToDTO(chat);
    }

    @Transactional
    public DTOChat updateChat(Long id, DTOChat newChat, DiplomUserDetails userDetails) throws EntityException, AccessException {
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
                "user with username "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntChat chat=rChat.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "chat with id "+id+" not found",
                "Чат не найден",
                EntChat.class
        ));
        if(!chat.getCreateBy().equals(user)){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user tried to delete chat, which doesn't belong to him",
                    "Вы не можете изменять этот чат",
                    userDetails
            );
        }
        chat.setName(newChat.getName());
        if(newChat.getSubjectName()!=null){
            if(!newChat.getSubjectName().equals(chat.getSubjectId().getName())) {
                EntSubject subject = rSubject.findByName(newChat.getSubjectName()).orElse(null);
                chat.setSubjectId(subject);
            }
        }
        Set<String> existingLogins = chat.getChatUsers()
                .stream()
                .map(chatUser -> chatUser.getUserId().getLogin())
                .collect(Collectors.toSet());
        Set<String> newChatUserLogins = new HashSet<>(Arrays.asList(newChat.getMembers())).stream().filter(login -> !login.isEmpty()).collect(Collectors.toSet());
        Set<EntChatUser> chatUserList=new HashSet<>(chat.getChatUsers());
        //newChatUserLogins.removeAll(existingLogins);

        if(newChat.getGroupName()!=null){
            if(!newChat.getGroupName().equals(chat.getGroupId().getName())) {
                EntGroup group = rGroup.findByName(newChat.getGroupName()).orElse(null);
                newChatUserLogins.addAll(group.getUsers_list().stream()
                        .map(EntUser::getLogin).toList());
                chat.setGroupId(group);
            }
        }

        List<EntChatUser> usersToRemove = chatUserList.stream()
                .filter(cu -> !newChatUserLogins.contains(cu.getUserId().getLogin()) &&
                        !cu.getUserId().equals(user))
                .collect(Collectors.toList());

        Set<String> loginsToAdd=new HashSet<>(newChatUserLogins);
        loginsToAdd.removeAll(existingLogins);

         List<EntChatUser> newChatUsers=loginsToAdd.stream()
                .map(login -> {
                    EntUser entUser;
                    try {
                        entUser = rUser.findByLogin(login)
                                .orElseThrow(() -> new EntityException(
                                        HttpStatus.NOT_FOUND,
                                        "user with login "+login+" not found",
                                        "Пользователь не найден",
                                        EntUser.class
                                ));
                    } catch (EntityException e) {
                        System.err.println(e.getDescription());
                        throw new RuntimeException(e);
                    }
                    return FactoryChatUser.createDefault(chat, entUser);
                }).toList();
        List<EntChatUser> finalUsers = new ArrayList<>(chatUserList);
        finalUsers.removeAll(usersToRemove);
        finalUsers.addAll(newChatUsers);

         if(!finalUsers.stream().map(EntChatUser::getUserId).toList().contains(user)){
             finalUsers.add(
                     rChatUser.findByChatIdAndUserId(chat,user).orElse(FactoryChatUser.createDefault(chat, user))
             );
         }
        chat.setChatUsers(new ArrayList<>(finalUsers));
        rChat.save(chat);
        if(!usersToRemove.isEmpty()){
            rChatUser.deleteAll(usersToRemove);
        }
        return ConverterChatToChat.convertEntityToDTO(chat);
    }
}
