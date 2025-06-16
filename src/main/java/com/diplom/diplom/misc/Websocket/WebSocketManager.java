package com.diplom.diplom.misc.Websocket;

import com.diplom.diplom.misc.Websocket.janus.WebSocketJanus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketManager {
    private final Map<Long, WebSocketJanus> userWebsockets = new ConcurrentHashMap<>();

    public void register(Long id, WebSocketJanus client) {
        userWebsockets.put(id, client);
    }

    public WebSocketJanus get(Long id) {
        return userWebsockets.get(id);
    }

    public void unregister(Long id) {
        WebSocketJanus client = userWebsockets.remove(id);
        if (client != null) {
            client.disableKeepAliveRequests();
            client.close();
        }
    }

    public boolean isConnected(Long id) {
        return id!=null && userWebsockets.containsKey(id);
    }

    public int connectionsCount() {
        return userWebsockets.size();
    }
}
