package com.diplom.diplom.service.videocalls;

import com.diplom.diplom.configuration.ConfUrls;
import com.diplom.diplom.entity.EntVideocalls;
import com.diplom.diplom.exception.JanusAPIException;
import com.diplom.diplom.misc.Websocket.janus.WebSocketJanus;
import com.diplom.diplom.misc.Websocket.WebSocketManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ServiceVideocallsJanusAPI {
    private final ConfUrls appurls;
    private final WebSocketManager wsManager;

    @Autowired
    public ServiceVideocallsJanusAPI(ConfUrls appurls, WebSocketManager wsManager) {
        this.appurls = appurls;
        this.wsManager = wsManager;
    }

    public WebSocketJanus connectToJanus(EntVideocalls videocall) throws URISyntaxException, InterruptedException, ExecutionException, JanusAPIException {
        if(wsManager.isConnected(videocall.getId())){
            throw new JanusAPIException(HttpStatus.CONFLICT,"videocall "+videocall.getId()+" is already connected","Пользователь уже подключен");
        }
        WebSocketJanus ws=new WebSocketJanus(appurls.wsJanus);
        ws.connect();
        JSONObject createSession = new JSONObject()
                .put("janus", "create")
                .put("transaction", UUID.randomUUID().toString());
        long connectionTimer= System.currentTimeMillis();
        while (!ws.isConnected() && System.currentTimeMillis()-connectionTimer < 10000) {
            Thread.sleep(100);
        }
        JSONObject createResponse = ws.sendRequest(createSession).get();
        long sessionId = createResponse.getJSONObject("data").getLong("id");
        videocall.setSessionId(sessionId);
        JSONObject attach = new JSONObject()
                .put("janus", "attach")
                .put("plugin", "janus.plugin.videoroom")
                .put("transaction", UUID.randomUUID().toString())
                .put("session_id", sessionId);
        JSONObject attachResponse = ws.sendRequest(attach).get();
        long handleId = attachResponse.getJSONObject("data").getLong("id");
        videocall.setHandleId(handleId);
        ws.enableKeepAliveRequests(sessionId);
        createRoom(videocall,ws);
        return ws;
    }

    private void createRoom(EntVideocalls videocall, WebSocketJanus ws) throws ExecutionException, InterruptedException {
        int roomCapacity=videocall.getConferencesId().getGroupId().stream().mapToInt(group -> group.getUsers_list().size()).sum();
        JSONObject body = new JSONObject()
                .put("request","create")
                .put("publishers", roomCapacity)
                .put("bitrate",512000)
                .put("audiocodec","opus")
                .put("audiolevel_ext",true)
                .put("audiolevel_event",true)
                .put("audio_active_packets",5)
                .put("audio_level_average",35)
                .put("audio_level_idle",60)
                .put("videocodec","vp8")
                .put("notify_joining",true);
        JSONObject newroom = new JSONObject()
                .put("janus", "message")
                .put("transaction", UUID.randomUUID().toString())
                .put("session_id",videocall.getSessionId())
                .put("handle_id",videocall.getHandleId())
                .put("body",body);
        JSONObject newroomRepsonse=ws.sendRequest(newroom).get();
        Long roomId=newroomRepsonse.getJSONObject("plugindata").getJSONObject("data").getLong("room");
        videocall.setRoomId(roomId);
    }
    
    public void deleteRoom(EntVideocalls videocall) throws ExecutionException, InterruptedException, JanusAPIException {
        if(!wsManager.isConnected(videocall.getId())){
            throw new JanusAPIException(HttpStatus.INTERNAL_SERVER_ERROR,"videocall "+videocall.getId()+" websocket is null","Ошибка связи с janus");
        }
        WebSocketJanus ws=wsManager.get(videocall.getId());
        JSONObject body = new JSONObject()
                .put("request", "destroy")
                .put("room", videocall.getRoomId());
        JSONObject destroy = new JSONObject()
                .put("janus","message")
                .put("transaction",UUID.randomUUID().toString())
                .put("session_id",videocall.getSessionId())
                .put("handle_id",videocall.getHandleId())
                .put("body",body);
        JSONObject destroyResponse=ws.sendRequest(destroy).get();
        if(destroyResponse.optString("janus").equals("error")){
            System.err.println("in destroy in room delete request");
            System.err.println(destroyResponse);
            throw new JanusAPIException(HttpStatus.INTERNAL_SERVER_ERROR,destroyResponse.toString(),"Ошибка удаления комнаты в janus");
        }
        wsManager.unregister(videocall.getId());
        System.out.println("connections:"+ wsManager.connectionsCount());
    }

    public boolean isWebsocketExist(EntVideocalls videocall){
        return wsManager.isConnected(videocall.getId());
    }

    public void saveWebsocket(WebSocketJanus ws, EntVideocalls videocall) {
        wsManager.register(videocall.getId(), ws);
    }
}
