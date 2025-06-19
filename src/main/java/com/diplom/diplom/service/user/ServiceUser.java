package com.diplom.diplom.service.user;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.dto.converter.ConverterUserUpdateToUser;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Generator;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.misc.utils.Parser;
import com.diplom.diplom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServiceUser {
    private final RepUser rUser;
    private final RepRole rRole;
    private final RepGroup rGroup;
    private final RepChat rChat;
    private final RepChatUser rChatUser;

    @Autowired
    public ServiceUser(RepUser rUser, RepRole rRole, RepGroup rGroup, RepChat rChat, RepChatUser rChatUser) {
        this.rUser = rUser;
        this.rRole = rRole;
        this.rGroup = rGroup;
        this.rChat = rChat;
        this.rChatUser = rChatUser;
    }

    public List<DTOUserUpdate> getUsersAll(int page){
        Pageable pageable = PageRequest.of(page, 20);
        List<EntUser> users= rUser.findAll(pageable).getContent();
        List<DTOUserUpdate> userList=new ArrayList<>();
        for(EntUser user:users){
            userList.add(new DTOUserUpdate(
                    user.getId(),
                    user.getLogin(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getSurname()
            ));
        }
        return userList;
    }

    public List<DTOUserUpdate> getUsersWithGroups(int page){
        Pageable pageable = PageRequest.of(page, 20);
        List<EntUser> users= rUser.findAll(pageable).getContent();
        List<DTOUserUpdate> userList=new ArrayList<>();
        for(EntUser user:users){
            userList.add(new DTOUserUpdate(
                    user.getId(),
                    user.getLogin(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getSurname(),
                    user.getGroups().stream().map(EntGroup::getName).collect(Collectors.toList())
            ));
        }
        return userList;
    }

    public List<DTOUserUpdate> getUsersDTO(){
        List<EntUser> users= (List<EntUser>) rUser.findAll();
        List<DTOUserUpdate> userList=new ArrayList<>();
        for(EntUser user:users){
            DTOUserUpdate dtoUser=new DTOUserUpdate(
                    user.getId(),
                    user.getLogin(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getSurname(),
                    user.getDateofbirth(),
                    user.getEmail(),
                    user.getQwestion().toString(),
                    user.getQwestionanswer(),
                    user.getStudentcard(),
                    (user.getGroups()!=null ? user.getGroups().stream().map(EntGroup::getName).toList() : null),
                    new ArrayList<>(user.getRoles().stream().map(EntRole::getName).toList())
            );
            userList.add(dtoUser);
        }
        return userList;
    }

    public List<EntUser> getUsersByRole(String role) throws EntityException {
        EntRole userrole=rRole.findByName(role).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "role not found when getting users "+role,
                "Роль не найдена",
                EntRole.class
        ));
        List<EntRole> roleList=new ArrayList<>();
        roleList.add(userrole);
      return rUser.findAllByRolesIn(roleList);
    }

    public DTOUserUpdate getFullDTOUserById(Long id) throws EntityException {
        EntUser user=rUser.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when getting by id "+id,
                "Пользователь не найден",
                EntUser.class
        ));
        return new DTOUserUpdate(
                user.getId(),
                user.getLogin(),
                user.getFirstname(),
                user.getLastname(),
                user.getSurname(),
                user.getDateofbirth(),
                user.getEmail(),
                user.getQwestion().toString(),
                user.getQwestionanswer(),
                user.getStudentcard(),
                (user.getGroups()!=null ? user.getGroups().stream().map(EntGroup::getName).toList() : null),
                new ArrayList<>(user.getRoles().stream().map(EntRole::getName).toList())
        );
    }

    public EntUser getOtherUserProfile(Long id) throws EntityException {
        EntUser user = rUser.findById(id).orElseThrow(() -> new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when getting by id " + id,
                "Пользователь не найден",
                EntUser.class
        ));
        EntUser userRet = new EntUser();
        userRet.setId(user.getId());
        userRet.setLogin(user.getLogin());
        userRet.setFirstname(user.getFirstname());
        userRet.setLastname(user.getLastname());
        userRet.setSurname(user.getSurname());
        userRet.setDateofbirth(user.getDateofbirth());
        userRet.setGroups(user.getGroups());
        userRet.setRoles(user.getRoles());
        return userRet;
    }

    public EntUser getUserById(Long id) throws EntityException {
        return rUser.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when getting by id "+id,
                "Пользователь не найден",
                EntUser.class
        ));
    }

    @Transactional
    public EntUser getUserByUsername(String username) throws EntityException {
        EntUser user=rUser.findByLogin(username).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when getting by username "+username,
                "Пользователь не найден",
                EntUser.class
        ));
        user.getRoles().size();
        return user;
    }

    public EntUser getUserByLogin(UserDetails userDetails) throws EntityException {
        return rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when getting by username "+userDetails.getUsername(),
                "Пользователь не найден",
                EntUser.class
        ));
    }

    public EntUser checkUserExistForPWRform(EntUser pwruser) throws EntityException {
        EntUser user=rUser.findByStudentcard(pwruser.getStudentcard()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when restoring password",
                "Пользователь не найден",
                EntUser.class
        ));
        if(user.getQwestionanswer().equals(pwruser.getQwestionanswer())){
            return user;
        }
        return null;
    }

    @Transactional
    public EntUser addUser(EntUser newuser, String[] groupname) throws EntityException {
        Optional<EntUser> user=rUser.findByLogin(newuser.getLogin());
        if(user.isPresent()){
            throw new EntityException(
                    HttpStatus.NOT_FOUND,
                    "user "+newuser.getLogin()+" aldready exist",
                    "Пользователь уже существует",
                    EntRole.class
            );
        }
        EntRole role=rRole.findByName("ROLE_STUDENT").orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "role not found when adding user",
                "Роль не найдена",
                EntRole.class
        ));
        String login=newuser.getLogin();
        String fname = newuser.getFirstname();
        String lname = newuser.getLastname();
        String surname = newuser.getSurname();
        String email = newuser.getEmail();
        newuser.setLogin(Parser.parseXssText(login));
        newuser.setFirstname(Parser.parseXssText(fname));
        newuser.setLastname(Parser.parseXssText(lname));
        newuser.setSurname(Parser.parseXssText(surname));
        newuser.setEmail(Parser.parseXssText(email));
        List<EntRole> roles=new ArrayList<>();
        roles.add(role);
        newuser.setRoles(roles);
        newuser.setPassword(Generator.generateCryptedPassword(newuser.getPassword()));
        List<EntGroup> groups=rGroup.findAllByNameIn(groupname);
        groups=groups.stream().filter(g -> !Objects.requireNonNull(g).getPrivateGroup()).toList();
        newuser.setGroups(groups);
        rUser.save(newuser);
        if(!groups.isEmpty()){
            List<EntChatUser> member=new ArrayList<>();
            for(EntGroup group:groups){
                List<EntChat> groupChats=rChat.findAllByGroupId(group);
                for(EntChat chat:groupChats){
                    member.add(new EntChatUser(null,chat,newuser));
                }
            }
            rChatUser.saveAll(member);
        }
        return newuser;
    }

    @Transactional
    public EntUser updUser(DTOUserUpdate dtouser, Long id, String[] groupname, DiplomUserDetails userDetails, boolean admin) throws AccessException, EntityException {
        EntUser user=rUser.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when updating with id "+id,
                "Пользователь не найден",
                EntUser.class
        ));
        EntUser newuser= ConverterUserUpdateToUser.convertDTOtoUser(dtouser);
        if(!(Checker.checkUserIdentity(userDetails,user,rUser) || (Checker.isUserHasRole(userDetails,rUser)))){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        if (newuser.getPassword() != null && !admin) {
            user.setPassword(Generator.generateCryptedPassword(newuser.getPassword()));
        } else {
            user.setPassword(user.getPassword());
        }
        Optional<EntUser> u= rUser.findByLogin(Parser.parseXssText(newuser.getLogin()));
        if(u.isPresent()){
            throw new EntityException(
                    HttpStatus.CONFLICT,
                    "user already exists",
                    "Логин "+newuser.getLogin()+" занят",
                    EntUser.class
            );
        }
        user.setLogin(Parser.parseXssText(newuser.getLogin()));
        user.setEmail(Parser.parseXssText(newuser.getEmail()));
        List<EntGroup> groups=rGroup.findAllByNameIn(groupname);
        if(!groups.isEmpty()){
            List<EntChatUser> member=new ArrayList<>();
            List<EntChatUser> toRemove=new ArrayList<>();
            List<EntGroup> oldGroups=user.getGroups();
            for(EntGroup group:oldGroups){
                List<EntChat> groupChats=rChat.findAllByGroupId(group);
                for(EntChat chat:groupChats){
                    Optional<EntChatUser> chatUser=rChatUser.findByChatIdAndUserId(chat,user);
                    chatUser.ifPresent(toRemove::add);
                }
            }
            for(EntGroup group:groups){
                List<EntChat> groupChats=rChat.findAllByGroupId(group);
                for(EntChat chat:groupChats){
                    member.add(new EntChatUser(null,chat,user));
                }
            }
            if(!toRemove.isEmpty()){
                rChatUser.deleteAll(toRemove);
            }
            if(!member.isEmpty()) {
                rChatUser.saveAll(member);
            }
        }
        user.setGroups(groups);
        user.setDateofbirth(newuser.getDateofbirth());
        if(!admin) {
            user.setQwestion(newuser.getQwestion());
            user.setQwestionanswer(newuser.getQwestionanswer());
        }
        user.setFirstname(Parser.parseXssText(newuser.getFirstname()));
        user.setLastname(Parser.parseXssText(newuser.getLastname()));
        user.setSurname(Parser.parseXssText(newuser.getSurname()));
        user.setStudentcard(newuser.getStudentcard());
        if(admin){
            List<EntRole> roleList=rRole.findByNameIn(dtouser.getUserRoles());
            user.setRoles(roleList);
        }
        rUser.save(user);
        return user;
    }

    @Transactional
    public EntUser updUserByPwRestore(EntUser newuser, Long id) throws AccessException, EntityException {
        EntUser user=rUser.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when updating with id "+id,
                "Пользователь не найден",
                EntUser.class
        ));
        user.setPassword(Generator.generateCryptedPassword(newuser.getPassword()));
       // user.setLogin(newuser.getLogin());
       // user.setEmail(newuser.getEmail());
       // user.setRoles(newuser.getRoles());
       // user.setGroups(user.getGroups());
       // user.setDateofbirth(newuser.getDateofbirth());
        //user.setQwestion(newuser.getQwestion());
        //user.setQwestionanswer(newuser.getQwestionanswer());
        //user.setFirstname(newuser.getFirstname());
        //user.setLastname(newuser.getLastname());
        //user.setSurname(newuser.getSurname());
        //user.setStudentcard(newuser.getStudentcard());
        rUser.save(user);
        return user;
    }

    @Transactional
    public DTOUserUpdate delUserById(Long id,DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntUser user=rUser.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when deleting by id "+id,
                "Пользователь не найден",
                EntUser.class
        ));
        if(!(Checker.checkUserIdentity(userDetails,user,rUser) || (Checker.isUserHasRole(userDetails,rUser)))){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        rUser.delete(user);
        return ConverterUserUpdateToUser.convertUserToDTO(user);
    }
}
