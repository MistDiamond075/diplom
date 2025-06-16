package com.diplom.diplom.service.videocalls;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.DTOVideocallUpdate;
import com.diplom.diplom.entity.EntVideocallChat;
import com.diplom.diplom.entity.EntVideocalls;
import com.diplom.diplom.entity.EntVideocallsHasUser;
import com.diplom.diplom.exception.*;
import com.diplom.diplom.misc.Websocket.videocall.WebSocketVideocall;
import com.diplom.diplom.repository.RepVideocalls;
import com.diplom.diplom.repository.RepVideocallsHasUser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service
@EnableAsync
public class ServiceVideocallsAsync {
    private final RepVideocalls rVideocalls;
    private final RepVideocallsHasUser rVideocallsHasUser;
    private final ServiceVideocalls srvVideocalls;
    private final ServiceVideocallsChat srvVideocallChat;

    @Autowired
    public ServiceVideocallsAsync(RepVideocalls rVideocalls, RepVideocallsHasUser rVideocallsHasUser, ServiceVideocalls srvVideocalls, ServiceVideocallsChat srvVideocallsChat) {
        this.rVideocalls = rVideocalls;
        this.rVideocallsHasUser = rVideocallsHasUser;
        this.srvVideocalls = srvVideocalls;
        this.srvVideocallChat = srvVideocallsChat;
    }

    @Async
    public CompletableFuture<ResponseEntity<?>> joinVideocalls(Long videocallId, DiplomUserDetails userDetails) throws AccessException, EntityException, URISyntaxException, ExecutionException, JanusAPIException, InterruptedException {
            EntVideocallsHasUser videocallsHasUser=srvVideocalls.joinVideocalls(videocallId, userDetails);
        return CompletableFuture.supplyAsync(() -> {
                    sendVideocallsHasUserParticipants(videocallsHasUser.getVideocallsId());
                    return ResponseEntity.ok(videocallsHasUser);
                }).handle((result,ex) ->{
                    if (ex == null) return result;
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AccessException ae) {
                        return ResponseEntity.status(ae.getStatus())
                                .body(new JSONObject()
                                        .put("message", ae.getMsg_for_user())
                                        .put("userRole", ae.getUserRole())
                                );

                    }else if(cause instanceof EntityException ee) {
                        return ResponseEntity.status(ee.getStatus())
                                .body(new JSONObject()
                                        .put("message", ee.getMsg_for_user())
                                        .put("entityName", ee.getEntityClassName())
                                );
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new JSONObject().put("error","unrecognized"));
                });
    }

    @Async
    public CompletableFuture<ResponseEntity<?>> leaveVideocall(Long videocallId, ServiceVideocalls.LeaveReasons reason, DiplomUserDetails userDetails) throws AccessException, ExecutionException, InterruptedException, EntityException, JanusAPIException {
        EntVideocallsHasUser videocallsHasUser=srvVideocalls.leaveVideocalls(videocallId,reason, userDetails);
        return CompletableFuture.supplyAsync(() -> {
                    removeVideocallsHasUserParticipant(videocallsHasUser.getVideocalluserId().getId(),videocallsHasUser.getVideocallsId().getRoomId());
                    return ResponseEntity.ok(videocallsHasUser);
                }).handle((result,ex) ->{
                    if (ex == null) return result;
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AccessException ae) {
                        return ResponseEntity.status(ae.getStatus())
                                .body(new JSONObject()
                                        .put("message",ae.getMsg_for_user())
                                        .put("userRole",ae.getUserRole()));
                    }else if(cause instanceof EntityException ee) {
                        return ResponseEntity.status(ee.getStatus())
                                .body(new JSONObject()
                                        .put("message", ee.getMsg_for_user())
                                        .put("entityName", ee.getEntityClassName())
                                );
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new JSONObject().put("error","unrecognized"));
                });
    }

    @Async
    public CompletableFuture<ResponseEntity<?>> updateUserSelfByAction(Long videocallId, ServiceVideocalls.UpdateActions action, EntVideocallsHasUser.defaultStates state, DiplomUserDetails userDetails) throws AccessException, EntityException {
        DTOVideocallUpdate videocallsHasUser=srvVideocalls.updateUserSelfByAction(videocallId, action, state, userDetails);
        return CompletableFuture.supplyAsync(() -> {
                    sendUserUpdate(videocallsHasUser.getVideocallsHasUser(),videocallsHasUser.getState(),action,(action== ServiceVideocalls.UpdateActions.AUDIO ||
                            action== ServiceVideocalls.UpdateActions.VIDEO));
                    return ResponseEntity.ok(videocallsHasUser.getVideocallsHasUser());
                }).handle((result,ex) ->{
                    if (ex == null) return result;
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AccessException ae) {
                        return ResponseEntity.status(ae.getStatus())
                                .body(new JSONObject()
                                        .put("message",ae.getMsg_for_user())
                                        .put("userRole",ae.getUserRole()));
                    }else if(cause instanceof EntityException ee) {
                        return ResponseEntity.status(ee.getStatus())
                                .body(new JSONObject()
                                        .put("message", ee.getMsg_for_user())
                                        .put("entityName", ee.getEntityClassName())
                                );
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new JSONObject().put("error","unrecognized"));
                });
    }

    @Async
    public CompletableFuture<ResponseEntity<?>> updateUserOtherByAction(Long videocallId, Long userId, ServiceVideocalls.UpdateActions action, EntVideocallsHasUser.defaultStates state, DiplomUserDetails userDetails) throws AccessException, EntityException, DataProcessingException {
        DTOVideocallUpdate videocallsHasUser=srvVideocalls.updateUserOtherByAction(videocallId, userId, action, state, userDetails);
        return CompletableFuture.supplyAsync(() -> {
                    sendUserOtherUpdate(videocallsHasUser.getVideocallsHasUser(),videocallsHasUser.getState(),action,(action== ServiceVideocalls.UpdateActions.AUDIO ||
                            action== ServiceVideocalls.UpdateActions.VIDEO),videocallsHasUser.getBanned());
                    return ResponseEntity.ok(videocallsHasUser.getVideocallsHasUser());
                }).handle((result,ex) ->{
                    if (ex == null) return result;
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AccessException ae) {
                        return ResponseEntity.status(ae.getStatus())
                                .body(new JSONObject()
                                        .put("message",ae.getMsg_for_user())
                                        .put("userRole",ae.getUserRole()));
                    }
                    else if(cause instanceof EntityException ee) {
                        return ResponseEntity.status(ee.getStatus())
                                .body(new JSONObject()
                                        .put("message", ee.getMsg_for_user())
                                        .put("entityName", ee.getEntityClassName())
                                );
                    }else if(cause instanceof DataProcessingException de) {
                        return ResponseEntity.status(de.getStatus())
                                .body(new JSONObject()
                                        .put("message", de.getMsg_for_user())
                                );
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new JSONObject().put("error","unrecognized"));
                });
    }

    @Async
    public void removeVideocallsHasUserParticipant(Long userId,Long roomId){
        JSONObject wsResponse=new JSONObject()
                .put("event","disconnected")
                .put("eventType","videocall")
                .put("forced",false)
                .put("id",userId);
        WebSocketVideocall.broadcastParticipants(roomId,wsResponse.toString());
    }

    @Async
    public void sendVideocallsHasUserParticipants(EntVideocalls videocall){
        List<EntVideocallsHasUser> usersList= rVideocallsHasUser.findAllByVideocallsId(videocall).stream()
                .filter(EntVideocallsHasUser::getConnected)
                .toList();
        List<EntVideocallChat> messages=srvVideocallChat.getMessages(videocall);
        JSONArray bodyUsersArray=new JSONArray();
        for(EntVideocallsHasUser videocallUser:usersList){
            JSONObject bodyUser=new JSONObject()
                    .put("id",videocallUser.getVideocalluserId().getId())
                    .put("firstname",videocallUser.getVideocalluserId().getFirstname())
                    .put("lastname",videocallUser.getVideocalluserId().getLastname())
                    .put("surname",videocallUser.getVideocalluserId().getSurname())
                    .put("login",videocallUser.getVideocalluserId().getLogin())
                    .put("microphone",videocallUser.getMicrostate())
                    .put("camera",videocallUser.getCamstate())
                    .put("sound",videocallUser.getSoundstate())
                    .put("demo",videocallUser.getDemostate());
            bodyUsersArray.put(bodyUser);
        }
        JSONArray bodyMessagesArray = new JSONArray();
        if(!messages.isEmpty()) {
            messages.sort(Comparator.comparing(EntVideocallChat::getDate));
            for(EntVideocallChat message:messages){
                JSONObject bodyMessage=new JSONObject()
                        .put("id",message.getId())
                        .put("text",message.getMessage())
                        .put("timestamp",message.getDate())
                        .put("username",message.getVideocalluserId().getVideocalluserId().getLastname()+" "+message.getVideocalluserId().getVideocalluserId().getFirstname()+" "+message.getVideocalluserId().getVideocalluserId().getSurname())
                        .put("login",message.getVideocalluserId().getVideocalluserId().getLogin())
                        .put("replyToId",message.getReplyto()!=null ? message.getReplyto().getVideocalluserId().getId() : JSONObject.NULL)
                        .put("replyToName",message.getReplyto()!=null ? message.getReplyto().getVideocalluserId().getLogin() : JSONObject.NULL);
                bodyMessagesArray.put(bodyMessage);
            }
        }
        JSONObject wsResponse=new JSONObject()
                .put("event","connected")
                .put("eventType","videocall")
                .put("users",bodyUsersArray)
                .put("messageArray",bodyMessagesArray);
        WebSocketVideocall.setParticipants(videocall.getRoomId(),wsResponse.toString());
        WebSocketVideocall.broadcastParticipants(videocall.getRoomId(),wsResponse.toString());
    }

    @Async
    protected void sendUserUpdate(EntVideocallsHasUser videocallsHasUser, EntVideocallsHasUser.defaultStates state, ServiceVideocalls.UpdateActions action, boolean janus) {
        WebSocketVideocall.broadcastParticipant(
                videocallsHasUser.getVideocallsId().getRoomId(),
                videocallsHasUser.getVideocalluserId().getId(),
                generateRequestForUserUpdates(action.toString().toLowerCase(), state == EntVideocallsHasUser.defaultStates.ON, state,janus,true,null).toString()
        );
        WebSocketVideocall.broadcastParticipants(
                videocallsHasUser.getVideocallsId().getRoomId(),
                generateRequestForUserUpdates(action.toString().toLowerCase(), state== EntVideocallsHasUser.defaultStates.ON,state, janus,false,videocallsHasUser.getVideocalluserId().getId()).toString()
        );
    }

    @Async
    protected void sendUserOtherUpdate(EntVideocallsHasUser videocallsHasUser, EntVideocallsHasUser.defaultStates state, ServiceVideocalls.UpdateActions action, boolean janus, boolean isBanned) {
        if(isBanned){
            JSONObject response=new JSONObject()
                    .put("event","disconnected")
                    .put("eventType","videocall")
                    .put("forced",true)
                    .put("id",videocallsHasUser.getVideocalluserId().getId());
            WebSocketVideocall.broadcastParticipant(videocallsHasUser.getVideocallsId().getRoomId(),videocallsHasUser.getVideocalluserId().getId(),response.toString());
        }else {
            WebSocketVideocall.broadcastParticipant(
                    videocallsHasUser.getVideocallsId().getRoomId(),
                    videocallsHasUser.getVideocalluserId().getId(),
                    generateRequestForUserUpdates(action.toString().toLowerCase(), false,state, janus,true,null).toString()
            );
            WebSocketVideocall.broadcastParticipants(
               videocallsHasUser.getVideocallsId().getRoomId(),
               generateRequestForUserUpdates(action.toString().toLowerCase(), state== EntVideocallsHasUser.defaultStates.ON,state, janus,false,videocallsHasUser.getVideocalluserId().getId()).toString()
            );
        }
    }

    private JSONObject generateRequestForUserUpdates(String action, Boolean state, EntVideocallsHasUser.defaultStates default_state, boolean janus, boolean self, Long userId){
        JSONObject message=new JSONObject()
                .put("request","configure")
                .put(action,state);
        JSONObject data=new JSONObject()
                .put("message",message)
                .put("state",default_state);
        JSONObject request=new JSONObject()
                .put("event","configure")
                .put("eventType","videocall")
                .put("self",self)
                .put("type",janus ? "janus" : "other")
                .put("data",data);
        if(userId!=null) {
            request.put("userId", userId);
        }
        return request;
    }
}
