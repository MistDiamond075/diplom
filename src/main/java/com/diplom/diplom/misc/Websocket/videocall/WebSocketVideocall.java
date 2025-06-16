package com.diplom.diplom.misc.Websocket.videocall;

import com.diplom.diplom.service.videocalls.ServiceVideocalls;
import com.diplom.diplom.service.videocalls.ServiceVideocallsAsync;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketVideocall extends WebSocketServer {
    private static final Map<Long,String> roomId_participants= new ConcurrentHashMap<>();
    private static final Map<Long,ConcurrentHashMap<Long,WebSocket>> roomId_connections= new ConcurrentHashMap<>();
    private static final Map<Long,Integer> connections_pongMisses= new ConcurrentHashMap<>();
    private static final Map<Long,ConcurrentHashMap<Long,WebSocket>> chatId_connections= new ConcurrentHashMap<>();
    private static final Map<WebSocket, Long> connectionToUserId = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int maxPongMisses = 3;
    private static ServiceVideocalls srvVideocallsHasUser;
    private static ServiceVideocallsAsync srvVideocallsAsync;

    public WebSocketVideocall(int port) {
        super(new InetSocketAddress(port));
    }

    public static void setServices(ServiceVideocalls service1, ServiceVideocallsAsync service2) {
        srvVideocallsHasUser = service1;
        srvVideocallsAsync = service2;
    }

    public SSLContext createSSLContext() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, UnrecoverableKeyException, CertificateException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream("src/main/resources/keystore.p12"), "106245".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "106245".toCharArray());
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }

    public static void setParticipants(Long room_id,String participantsJson){
        if(room_id!=null) {
            roomId_participants.put(room_id, participantsJson);
        }
    }

    public static void broadcastParticipants(Long room_id,String participantJson) {
        if(room_id!=null) {
            if(!roomId_connections.containsKey(room_id)) {
                return;
            }
            List<Long> connectionsToDelete=new ArrayList<>();
            ConcurrentHashMap<Long,WebSocket> connections=roomId_connections.get(room_id);
            for (Map.Entry<Long,WebSocket> conn : connections.entrySet()) {
                try {
                    conn.getValue().send(participantJson);
                } catch (Exception e) {
                    connectionsToDelete.add(conn.getKey());
                }
            }
            if(!connectionsToDelete.isEmpty()) {
                for (Long connect : connectionsToDelete) {
                    connections.remove(connect);
                }
                roomId_connections.put(room_id, connections);
            }
        }
    }

    public static void broadcastParticipant(Long room_id,Long user_id,String participantJson) {
        if(room_id!=null && user_id!=null) {
            ConcurrentHashMap<Long,WebSocket> connections=roomId_connections.get(room_id);
            WebSocket conn=connections.get(user_id);
            try {
                conn.send(participantJson);
            } catch (Exception e) {
                connections.remove(user_id);
                roomId_connections.put(room_id,connections);
            }
        }
    }

    public static void sendMessageToChat(Long chatId,String response){
        if(chatId!=null) {
            if(!chatId_connections.containsKey(chatId)) {
                return;
            }
            List<Long> connectionsToDelete=new ArrayList<>();
            ConcurrentHashMap<Long,WebSocket> connections=chatId_connections.get(chatId);
            for (Map.Entry<Long,WebSocket> conn : connections.entrySet()) {
                try {
                    conn.getValue().send(response);
                } catch (Exception e) {
                    connectionsToDelete.add(conn.getKey());
                }
            }
            if(!connectionsToDelete.isEmpty()) {
                for (Long connect : connectionsToDelete) {
                    connections.remove(connect);
                }
                chatId_connections.put(chatId, connections);
            }
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake serverHandshake) {
        String path=serverHandshake.getResourceDescriptor();
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn,String request) {
        JSONObject obj = new JSONObject(request);
        String event=obj.getString("event");
        String eventType=obj.getString("eventType");
        if(Objects.equals(eventType, "videocall")) {
            if (event.equals("joined")) {
                Long roomId = obj.optLong("roomId");
                Long userId = obj.getLong("userId");
                if (!roomId_connections.containsKey(roomId)) {
                    roomId_connections.put(roomId, new ConcurrentHashMap<>());
                }
                if(!connectionToUserId.containsKey(conn)) {
                    connectionToUserId.put(conn, userId);
                }
                roomId_connections.get(roomId).put(userId, conn);
                String response = roomId_participants.get(roomId);
                if (response != null) {
                    conn.send(response);
                }
            } else if (event.equals("leave")) {
                Long roomId = obj.getLong("roomId");
                roomId_participants.remove(roomId);
                Long userId = obj.getLong("userId");
                roomId_connections.get(roomId).remove(userId);
                connections_pongMisses.remove(userId);
            }
        }else{
            Long userId = obj.getLong("userId");
            Long chatId= obj.getLong("chatId");
            if(!isConnected(userId)){
                if(!chatId_connections.containsKey(chatId)) {
                    ConcurrentHashMap<Long, WebSocket> connection = new ConcurrentHashMap<>();
                    connection.put(userId, conn);
                    chatId_connections.put(chatId, connection);
                }else{
                    chatId_connections.get(chatId).put(userId, conn);
                }
                if(!connectionToUserId.containsKey(conn)) {
                    connectionToUserId.put(conn, userId);
                }
            }else if (event.equals("leave")) {
                chatId_connections.get(chatId).remove(userId);
                connections_pongMisses.remove(userId);
            }else if(event.equals("switch")){
                Optional<Long> foundChatId = chatId_connections.entrySet().stream()
                        .filter(entry -> entry.getValue().containsKey(userId))
                        .map(Map.Entry::getKey)
                        .findFirst();
                foundChatId.ifPresent(aLong -> chatId_connections.get(aLong).remove(userId));
                if(!chatId_connections.containsKey(chatId)) {
                    chatId_connections.put(chatId, new ConcurrentHashMap<>());
                }
                chatId_connections.get(chatId).put(userId, conn);
            }
        }
        if(event.equals("pong")){
            Long userId=obj.getLong("userId");
            connections_pongMisses.put(userId,0);
        }
    }

    @Override
    public void onClose(WebSocket conn,int code, String reason, boolean b) {
        System.out.println("WebSocket closed with code "+code+": " + reason);
        Long userId = connectionToUserId.get(conn);

        if (userId == null) {
            System.out.println("Connection was not tracked.");
            return;
        }

        boolean found = false;
        for (Map.Entry<Long, ConcurrentHashMap<Long, WebSocket>> entry : roomId_connections.entrySet()) {
            if (entry.getValue().remove(userId) != null) {
                found = true;
                System.out.println("Removed user " + userId + " from conference room " + entry.getKey());
                if (entry.getValue().isEmpty()) {
                    roomId_connections.remove(entry.getKey());
                }
                break;
            }
        }
        if (found) {
            connections_pongMisses.remove(userId);
            return;
        }
        for (Map.Entry<Long, ConcurrentHashMap<Long, WebSocket>> entry : chatId_connections.entrySet()) {
            if (entry.getValue().remove(userId) != null) {
                System.out.println("Removed user " + userId + " from chat " + entry.getKey());
                if (entry.getValue().isEmpty()) {
                    chatId_connections.remove(entry.getKey());
                }
                break;
            }
        }

        connections_pongMisses.remove(userId);
    }

    @Override
    public void onError(WebSocket conn,Exception e) {
        System.err.println("WebSocket error: " + e.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket started");
        pingScheduler.scheduleAtFixedRate(() -> {
            for (Map.Entry<Long,ConcurrentHashMap<Long,WebSocket>> conn : roomId_connections.entrySet()) {
                for (Map.Entry<Long,WebSocket> conn2 : conn.getValue().entrySet()) {
                    if (conn2.getValue().isOpen()) {
                        try {
                            JSONObject ping = new JSONObject().put("event", "ping").put("eventType","videocall");
                            conn2.getValue().send(ping.toString());
                        } catch (Exception e) {
                            System.err.println("Failed to send ping to client: " + e.getMessage());
                        }finally {
                            if(!connections_pongMisses.containsKey(conn2.getKey())) {
                                connections_pongMisses.put(conn2.getKey(),0);
                            }
                            connections_pongMisses.merge(conn2.getKey(), 1,Integer::sum);
                            if(connections_pongMisses.get(conn2.getKey())>maxPongMisses) {
                                conn2.getValue().close();
                                roomId_connections.remove(conn.getKey());
                                if(srvVideocallsHasUser!=null){
                                    srvVideocallsHasUser.updateUserConnectionStatus(conn2.getKey(),false);
                                    srvVideocallsAsync.removeVideocallsHasUserParticipant(conn2.getKey(),conn.getKey());
                                }
                            }
                        }
                    }
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private boolean isConnected(Long userId) {
        return roomId_connections.values().stream().anyMatch(connections -> connections.containsKey(userId)) ||
        chatId_connections.values().stream().anyMatch(connections -> connections.containsKey(userId));
    }
}
