package com.diplom.diplom.service.videocalls;

import com.diplom.diplom.configuration.ConfUrls;
import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOVideocallUpdate;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.*;
import com.diplom.diplom.misc.Websocket.janus.WebSocketJanus;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.misc.utils.Parser;
import com.diplom.diplom.repository.*;
import jakarta.persistence.LockTimeoutException;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ServiceVideocalls {
    private final RepVideocalls rVideocalls;
    private final RepUser rUser;
    private final RepConferences rConferences;
    private final RepVideocallsHasUser rVideocallsHasUser;
    private final RepVideocallsHasUserProperties rVideocallsHasUserProperties;
    private final ServiceVideocallsJanusAPI srvVideocallsJanusAPI;

    public enum UpdateActions {AUDIO, VIDEO, DEMONSTRATION,REACTION,BAN,SOUND}
    public enum LeaveReasons {EXIT,RELOAD}

    @Autowired
    public ServiceVideocalls(RepVideocalls rVideocalls, RepUser rUser, RepConferences rConferences, RepVideocallsHasUser rVideocallsHasUser, RepVideocallsHasUserProperties rVideocallsHasUserProperties, ServiceVideocallsJanusAPI srvVideocallsJanusAPI, ConfUrls appurls) {
        this.rVideocalls = rVideocalls;
        this.rUser = rUser;
        this.rConferences = rConferences;
        this.rVideocallsHasUser = rVideocallsHasUser;
        this.rVideocallsHasUserProperties = rVideocallsHasUserProperties;
        this.srvVideocallsJanusAPI = srvVideocallsJanusAPI;
    }

    public List<EntVideocalls> getVideocalls(){
        return rVideocalls.findAll();
    }

    public EntVideocalls getVideocallById(Long id) throws EntityException {
        return rVideocalls.findById(id).orElseThrow(()->new EntityException(HttpStatus.NOT_FOUND,"videocall not found by id: " + id,"Видеозвонок не найден", EntVideocalls.class));
    }

    public List<EntVideocalls> getVideocallsByConferencesCreator(EntUser user, DiplomUserDetails userDetails) throws AccessException {
        if(!Checker.checkUserIdentity(userDetails,user,rUser)){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        return rVideocalls.findAllByConferencesId_Createdby(user);
    }

    public List<EntVideocalls> getVideocallsByGroup(EntUser user){
        return rVideocalls.findAllByConferencesId_GroupIdIn(user.getGroups());
    }

    public EntVideocallsHasUser getVideocallsHasUserByUserDetailsAndVideocallId(UserDetails userDetails, Long videocallId) throws EntityException, AccessException {
        if(userDetails==null){
            throw new AccessException(HttpStatus.BAD_REQUEST,"user details was null","Ошибка данных вашего аккаунта", null);
        }
        EntUser user=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,"user not found when getting videocallsHasUser",
                "Пользователь не найден",
                EntUser.class
                ));
        EntVideocalls videocall=rVideocalls.findById(videocallId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall not found by id: " + videocallId,
                "Видеозвонок не найден",
                EntVideocalls.class
        ));
        return rVideocallsHasUser.findByVideocalluserId(user,videocall).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocallHasUser not found by user with username "+ userDetails.getUsername() +" and videocall id "+(videocall != null ? videocall.getId() : "undefined"),
                "Участник конференции не найден",
                EntVideocallsHasUser.class
        ));
    }

    @Transactional(rollbackOn = {BaseException.class})
    @Retryable(
            retryFor = { PessimisticLockingFailureException.class, LockTimeoutException.class },
            backoff = @Backoff(delay = 200),
            maxAttempts = 5
    )
    public EntVideocallsHasUser joinVideocalls(Long videocallId, DiplomUserDetails userDetails) throws AccessException, EntityException, URISyntaxException, ExecutionException, JanusAPIException, InterruptedException {
        if(userDetails==null){
            throw new AccessException(HttpStatus.BAD_REQUEST,"user details was null","Ошибка данных вашего аккаунта", null);
        }
        EntUser user =rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userDetails.getUsername()+" not found when join to videocall with id "+videocallId,
                "Данные вашего аккаунта не найдены",
                EntUser.class
        ));
        EntVideocalls videocall=rVideocalls.findByIdwithLock(videocallId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall not found by id: "+videocallId,
                "Видеозвонок не найден",
                EntVideocalls.class
        ));
        List<EntVideocallsHasUser> existingUser=rVideocallsHasUser.findAllByVideocalluserId(user);
        if(!existingUser.isEmpty()){
            for(EntVideocallsHasUser v:existingUser){
                if(v.getConnected()){
                    throw new AccessException(HttpStatus.BAD_REQUEST,"user tried to connect to vide ocall when them was already connected","Вы уже подключены к другой видеоконференции", null);
                }
            }
        }
        Optional<EntVideocallsHasUser> videocalldataFromDb=rVideocallsHasUser.findByVideocalluserId(user,videocall);
        EntVideocallsHasUser videocallhasuser;
        if(videocalldataFromDb.isEmpty()) {
            videocallhasuser=new EntVideocallsHasUser(null,videocall,user, EntVideocallsHasUser.defaultStates.OFF, EntVideocallsHasUser.defaultStates.OFF, EntVideocallsHasUser.defaultStates.ON, EntVideocallsHasUser.defaultStates.OFF,false, true);
            rVideocallsHasUser.save(videocallhasuser);
        }else {
            videocallhasuser=videocalldataFromDb.get();
            videocallhasuser.setConnected(true);
            EntVideocallsHasUserProperties properties=rVideocallsHasUserProperties.findByvideocallHasUserId(videocallhasuser).orElse(null);
            if(properties!=null && properties.isBanned()) {
                throw new AccessException(
                        HttpStatus.FORBIDDEN,
                        "user with id "+(videocallhasuser.getVideocalluserId()!=null ? videocallhasuser.getVideocalluserId().getId() : "undefined")+
                        " trying to connect to videocall with id "+(videocallhasuser.getVideocallsId()!=null ? videocallhasuser.getVideocallsId().getId() : "undefined") + " where he was banned in videocall",
                        "Ошибка доступа: вы были забанены в этом звонке",
                        userDetails
                );
            }
        }
        rVideocallsHasUser.save(videocallhasuser);
        videocall.setParticipants(videocall.getParticipants()+1);
        if(!srvVideocallsJanusAPI.isWebsocketExist(videocall)) {
            WebSocketJanus ws=srvVideocallsJanusAPI.connectToJanus(videocall);
            srvVideocallsJanusAPI.saveWebsocket(ws,videocall);
        }
        rVideocalls.save(videocall);
        return videocallhasuser;
    }

    @Transactional(rollbackOn = {BaseException.class, ExecutionException.class,InterruptedException.class})
    @Retryable(
            retryFor = { PessimisticLockingFailureException.class, LockTimeoutException.class },
            backoff = @Backoff(delay = 200),
            maxAttempts = 5
    )
    public EntVideocallsHasUser leaveVideocalls(Long videocallId, LeaveReasons reason, DiplomUserDetails userDetails) throws AccessException, ExecutionException, InterruptedException, EntityException, JanusAPIException {
        if(userDetails==null){
            throw new AccessException(HttpStatus.BAD_REQUEST,"user details was null","Ошибка данных вашего аккаунта", null);
        }
        EntUser user =rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userDetails.getUsername()+" not found when join to videocall with id "+videocallId,
                "Пользователь не найден",
                EntUser.class
        ));
        EntVideocalls activecall=rVideocalls.findByIdwithLock(videocallId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall not found by id "+videocallId,
                "Видеозвонок не найден",
                EntVideocalls.class
        ));
        EntVideocallsHasUser videocallhasuser=rVideocallsHasUser.findByVideocalluserId(user,activecall).orElseThrow(()-> new EntityException(
                HttpStatus.NOT_FOUND,
                "videocallsHasUser not found by videocall id " +(activecall!=null ? activecall.getId() : "undefined") +" and user id "+(user!=null ? user.getId() : "undefined"),
                "Участник конференции не найден",
                EntVideocallsHasUser.class
        ));
        if(!videocallhasuser.getConnected()) {
            throw new AccessException(HttpStatus.FORBIDDEN,"user with id "+videocallhasuser.getVideocalluserId().getId()+ " trying to disconnect to videocall with id "+videocallhasuser.getVideocallsId().getId()
            +" where he was banned in videocall","Пользователь не подключен к видеоконференции",userDetails);
        }
        videocallhasuser.setConnected(false);
        rVideocallsHasUser.save(videocallhasuser);
        int participans=activecall.getParticipants()-1;
        activecall.setParticipants(participans);
        if(reason!=LeaveReasons.RELOAD) {
            if (participans <= 0) {
                rVideocallsHasUser.deleteAllByVideocallsId(activecall);
                rVideocalls.delete(activecall);
                srvVideocallsJanusAPI.deleteRoom(activecall);
            } else {
                rVideocalls.save(activecall);
            }
        }
        return videocallhasuser;
    }

    @Transactional(rollbackOn = {BaseException.class, ExecutionException.class,InterruptedException.class})
    @Retryable(
            retryFor = { PessimisticLockingFailureException.class, LockTimeoutException.class },
            backoff = @Backoff(delay = 200)
    )
    public EntVideocalls addVideocall(Long conferenceId) throws URISyntaxException, ExecutionException, InterruptedException, EntityException, JanusAPIException {
        EntConferences conference=rConferences.findByIdwithLock(conferenceId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "conference with id "+conferenceId+" not found when adding videocall",
                "Конференция не найдена",
                EntConferences.class
        ));
        Optional<EntVideocalls> activecall=rVideocalls.findByConferencesId(conference);
        if(activecall.isEmpty()) {
            EntVideocalls videocall = new EntVideocalls(null, conference, null, 0);
            WebSocketJanus ws=srvVideocallsJanusAPI.connectToJanus(videocall);
            rVideocalls.save(videocall);
            srvVideocallsJanusAPI.saveWebsocket(ws,videocall);
            return videocall;
        }
        return activecall.get();
    }

    public EntVideocallsHasUser updateVideocallHasUser(EntVideocallsHasUser newvideocallsHasUser) throws EntityException, DataProcessingException {
        if(newvideocallsHasUser==null){
            throw new DataProcessingException(HttpStatus.BAD_REQUEST,"newvideocallsHasUser was null","Ошибка запроса: данные не могут быть null");
        }
        EntVideocallsHasUser videocallsHasUser=rVideocallsHasUser.findById(newvideocallsHasUser.getId()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall_has_user not found by id "+newvideocallsHasUser.getId(),
                "Участник конференции не найден",
                EntVideocallsHasUser.class
        ));
        videocallsHasUser.setVideocallsId(newvideocallsHasUser.getVideocallsId());
        videocallsHasUser.setSignalstate(newvideocallsHasUser.getSignalstate());
        videocallsHasUser.setConnected(newvideocallsHasUser.getConnected());
        videocallsHasUser.setDemostate(newvideocallsHasUser.getDemostate());
        videocallsHasUser.setSoundstate(newvideocallsHasUser.getSoundstate());
        videocallsHasUser.setCamstate(newvideocallsHasUser.getCamstate());
        videocallsHasUser.setMicrostate(newvideocallsHasUser.getMicrostate());
        videocallsHasUser.setVideocalluserId(newvideocallsHasUser.getVideocalluserId());
        rVideocallsHasUser.save(videocallsHasUser);
        return videocallsHasUser;
    }

    public EntVideocalls deleteVideocallById(Long id) throws EntityException {
        EntVideocalls videocall=rVideocalls.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall not found when deleting by id "+id,
                "Видеозвонок не найден",
                EntVideocalls.class
        ));
        rVideocalls.delete(videocall);
        return videocall;
    }

    @Transactional
    public DTOVideocallUpdate updateUserSelfByAction(Long videocallId, UpdateActions action, EntVideocallsHasUser.defaultStates state, DiplomUserDetails userDetails) throws AccessException, EntityException {
        if(userDetails==null){
            throw new AccessException(HttpStatus.BAD_REQUEST,"user details was null","Ошибка данных вашего аккаунта", null);
        }
        EntUser user=rUser.findByLogin(Objects.requireNonNull(userDetails.getUsername())).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with username "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntVideocalls videocall=rVideocalls.findById(videocallId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall not found by id "+videocallId+" when updating videocallHasUser by action",
                "Видеозвонок не найден",
                EntVideocalls.class
        ));
        EntVideocallsHasUser videocallsHasUser=rVideocallsHasUser.findByVideocalluserIdwithLock(user,videocall).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall has user not found by videocall with id "+(videocall!=null ? videocall.getId() : "undefined")+" and user with id "+(user!=null ? user.getId(): "undefined"),
                "Участник конференции не найден",
                EntVideocallsHasUser.class
        ));
        EntVideocallsHasUserProperties properties=rVideocallsHasUserProperties.findByvideocallHasUserId(videocallsHasUser).orElse(null);
        if(!videocallsHasUser.getConnected()){
            throw new EntityException(
                    HttpStatus.NOT_FOUND,
                    "user with id "+(videocallsHasUser.getVideocalluserId()!=null ? videocallsHasUser.getVideocalluserId().getId(): "undefined")+" not connected to videocall with id "+
                            (videocallsHasUser.getVideocallsId()!=null ? videocallsHasUser.getVideocallsId() : "undefined"),
                    "Пользователь не подключён к конференции",
                    EntVideocallsHasUser.class
            );
        }
       boolean updated=false;
        EntVideocallsHasUser.defaultStates newstate= EntVideocallsHasUser.defaultStates.OFF;
        switch (action){
            case AUDIO -> {
                if(videocallsHasUser.getMicrostate()== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                    if(properties!=null && !Checker.isUserMorePowerful(videocallsHasUser.getVideocalluserId(), properties.getMicromuted())){
                        throw new AccessException(HttpStatus.BAD_REQUEST,"user with id "+videocallsHasUser.getVideocalluserId().getId()+ " trying to update his devices in videocall with id "+
                                videocallsHasUser.getVideocallsId().getId()
                                +" without enough role power","Ошибка доступа: недостаточно прав",userDetails);
                    }
                }
                updated= videocallsHasUser.getMicrostate()!=state;
                videocallsHasUser.setMicrostate(state);
                newstate=videocallsHasUser.getMicrostate();
            }
            case VIDEO -> {
                if(videocallsHasUser.getCamstate()== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                    if(properties!=null && !Checker.isUserMorePowerful(videocallsHasUser.getVideocalluserId(), properties.getCameramuted())){
                        throw new AccessException(HttpStatus.BAD_REQUEST,"user with id "+videocallsHasUser.getVideocalluserId().getId()+ " trying to update his "+action+" in videocall with id "+
                                videocallsHasUser.getVideocallsId().getId()
                                +" without enough role power","Ошибка доступа: недостаточно прав",userDetails);
                    }
                }
                updated= videocallsHasUser.getCamstate()!=state;
                videocallsHasUser.setCamstate(state);
                newstate=videocallsHasUser.getCamstate();
            }
            case DEMONSTRATION -> {
                if(videocallsHasUser.getDemostate()== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                    if(properties!=null && !Checker.isUserMorePowerful(videocallsHasUser.getVideocalluserId(), properties.getDemomuted())){
                        throw new AccessException(HttpStatus.BAD_REQUEST,"user with id "+videocallsHasUser.getVideocalluserId().getId()+ " trying to update his "+action+" in videocall with id "+
                                videocallsHasUser.getVideocallsId().getId()
                                +" without enough role power","Ошибка доступа: недостаточно прав",userDetails);
                    }
                }
                updated= true;
                videocallsHasUser.setDemostate(
                        (videocallsHasUser.getDemostate()== EntVideocallsHasUser.defaultStates.ON) ? EntVideocallsHasUser.defaultStates.OFF : EntVideocallsHasUser.defaultStates.ON
                );
                newstate=videocallsHasUser.getDemostate();
            }
            case REACTION -> {
                //updated= videocallsHasUser.getSignalstate()!=state;
               // videocallsHasUser.setSignalstate(state);
            }
            case SOUND -> {
                if(videocallsHasUser.getSoundstate()== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                     if(properties!=null && !Checker.isUserMorePowerful(videocallsHasUser.getVideocalluserId(), properties.getSoundmuted())){
                         throw new AccessException(HttpStatus.BAD_REQUEST,"user with id "+videocallsHasUser.getVideocalluserId().getId()+ " trying to update his "+action+" in videocall with id "+
                                 videocallsHasUser.getVideocallsId().getId()
                                 +" without enough role power","Ошибка доступа: недостаточно прав",userDetails);
                     }
                }
                updated= videocallsHasUser.getSoundstate()!=state;
                videocallsHasUser.setSoundstate(state);
                newstate=videocallsHasUser.getSoundstate();
            }
        }
        if(updated) {
            rVideocallsHasUser.save(videocallsHasUser);
        }
        return new DTOVideocallUpdate(videocallsHasUser,null,newstate);
    }

    @Transactional
    public DTOVideocallUpdate updateUserOtherByAction(Long videocallId, Long userId, UpdateActions action, EntVideocallsHasUser.defaultStates state, DiplomUserDetails userDetails) throws AccessException, EntityException, DataProcessingException {
        if(userDetails==null){
            throw new AccessException(HttpStatus.BAD_REQUEST,"user details was null","Ошибка данных вашего аккаунта", null);
        }
        EntUser userUpdater=rUser.findByLogin(Objects.requireNonNull(userDetails.getUsername())).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userDetails.getUsername()+" not found",
                "Ваш аккаунт не найден",
                EntUser.class
        ));
        EntUser userUpdated=rUser.findById(Objects.requireNonNull(userId)).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userId+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntVideocalls videocall=rVideocalls.findById(videocallId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall not found by id "+videocallId+" when updating videocallHasUser by action",
                "Видеозвонок не найден",
                EntVideocalls.class
        ));
        EntVideocallsHasUser videocallsHasUserUpdated=rVideocallsHasUser.findByVideocalluserIdwithLock(userUpdated,videocall).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "videocall has user not found by videocall with id "+(videocall!=null ? videocall.getId() : "undefined")+" and user with id "+(userUpdated!=null ? userUpdated.getId(): "undefined"),
                "Участник конференции не найден",
                EntVideocallsHasUser.class
        ));
        EntVideocallsHasUserProperties properties=rVideocallsHasUserProperties.findByvideocallHasUserId(videocallsHasUserUpdated).orElse(
                new EntVideocallsHasUserProperties(null,videocallsHasUserUpdated,0,0,0,0,false)
        );
        if(!videocallsHasUserUpdated.getConnected()){
            throw new AccessException(HttpStatus.BAD_REQUEST,"user with id "+userUpdater.getId()+ " trying to update non-connected user with id "+
                    videocallsHasUserUpdated.getVideocalluserId().getId()+" in videocall with id "+videocallsHasUserUpdated.getVideocallsId().getId()
                    ,"Ошибка запроса: изменяемый пользователь не подключен к конференции",userDetails);
        }
        EntVideocallsHasUser.defaultStates newstate=null;
        if(Checker.isUserMorePowerful(userUpdater,userUpdated,false)) {
            boolean isBanned=false;
            switch (action) {
                case AUDIO -> {
                    if(state==null){
                        throw new DataProcessingException(HttpStatus.BAD_REQUEST,"state is null","Ошибка запроса: state не может быть null для этого устройства");
                    }
                    if(state== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                        videocallsHasUserUpdated.setMicrostate(EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN);
                        properties.setMicromuted(Parser.parseUserRolesMaxPower(userUpdater));
                    }else{
                        if(videocallsHasUserUpdated.getMicrostate()!= EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN){
                            throw new AccessException(HttpStatus.BAD_REQUEST,"user with id "+userUpdater.getId()+ " trying to update non-muted user's "+action+" with id "+
                                    videocallsHasUserUpdated.getVideocalluserId().getId()+
                                    " in videocall with id "+videocallsHasUserUpdated.getVideocallsId().getId(),
                                    "Ошибка запроса: изменяемый пользователь не был заглушен администратором",userDetails);
                        }
                        properties.setMicromuted(0);
                        videocallsHasUserUpdated.setMicrostate(EntVideocallsHasUser.defaultStates.OFF);
                    }
                    newstate=videocallsHasUserUpdated.getMicrostate();
                }
                case VIDEO -> {
                    if(state==null){
                        throw new DataProcessingException(HttpStatus.BAD_REQUEST,"state is null","Ошибка запроса: state не может быть null для этого устройства");
                    }
                    if(state== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                        videocallsHasUserUpdated.setCamstate(EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN);
                        properties.setCameramuted(Parser.parseUserRolesMaxPower(userUpdater));
                    }else {
                        if(videocallsHasUserUpdated.getCamstate()!= EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN){
                            throw new AccessException(HttpStatus.BAD_REQUEST,"user with id "+userUpdater.getId()+ " trying to update non-muted user's "+action+" with id "+
                                    videocallsHasUserUpdated.getVideocalluserId().getId()+
                                    " in videocall with id "+videocallsHasUserUpdated.getVideocallsId().getId(),
                                    "Ошибка запроса: изменяемый пользователь не был заглушен администратором",userDetails);
                        }
                        properties.setCameramuted(0);
                        videocallsHasUserUpdated.setCamstate(EntVideocallsHasUser.defaultStates.OFF);
                    }
                    newstate=videocallsHasUserUpdated.getCamstate();
                }
                case DEMONSTRATION -> {
                    if(state== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                        videocallsHasUserUpdated.setDemostate(EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN);
                        properties.setDemomuted(Parser.parseUserRolesMaxPower(userUpdater));
                    }else {
                        videocallsHasUserUpdated.setDemostate(EntVideocallsHasUser.defaultStates.OFF);
                        properties.setDemomuted(0);
                    }
                    newstate=videocallsHasUserUpdated.getDemostate();
                }
                case REACTION -> {
                }
                case SOUND -> {
                    if(state== EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN) {
                        videocallsHasUserUpdated.setSoundstate(EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN);
                        properties.setSoundmuted(Parser.parseUserRolesMaxPower(userUpdater));
                    }else {
                        videocallsHasUserUpdated.setSoundstate(EntVideocallsHasUser.defaultStates.OFF);
                        properties.setSoundmuted(0);
                    }
                    newstate=videocallsHasUserUpdated.getSoundstate();
                }
                case BAN -> {
                    properties.setBanned(!properties.isBanned());
                    isBanned=true;
                }
            }
            rVideocallsHasUser.save(videocallsHasUserUpdated);
            rVideocallsHasUserProperties.save(properties);
            return new DTOVideocallUpdate(videocallsHasUserUpdated,isBanned,newstate);
        }else{
            throw new AccessException(
                    HttpStatus.FORBIDDEN,"user with role power "+(userUpdater!=null ? Parser.parseUserRolesMaxPower(userUpdater) : "undefined") +
                    " tried to update user with role power "+(userUpdated!=null ? Parser.parseUserRolesMaxPower(userUpdated) : "undefined"),
                    "Ошибка доступа: недостаточно прав",
                    userDetails
            );
        }
    }

    public void updateUserConnectionStatus(Long userId,boolean connected){
        rVideocallsHasUser.updateConnectionStatus(userId,connected);
    }
}
