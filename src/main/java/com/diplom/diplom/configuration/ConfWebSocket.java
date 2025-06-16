package com.diplom.diplom.configuration;

import com.diplom.diplom.misc.Websocket.videocall.WebSocketVideocall;
import com.diplom.diplom.service.videocalls.ServiceVideocalls;
import com.diplom.diplom.service.videocalls.ServiceVideocallsAsync;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfWebSocket {
    @Value("${url.ws.port}")
    private int wsPort;
    private final ServiceVideocalls srvVideocallsHasUser;
    private final ServiceVideocallsAsync srvVideocallsAsync;

    public ConfWebSocket(ServiceVideocalls srvVideocallsHasUser, ServiceVideocallsAsync srvVideocallsAsync) {
        this.srvVideocallsHasUser = srvVideocallsHasUser;
        this.srvVideocallsAsync = srvVideocallsAsync;
    }

    @Bean
    public WebSocketVideocall webSocketVideocall() {
        try {
            WebSocketVideocall ws = new WebSocketVideocall(wsPort);
            WebSocketVideocall.setServices(srvVideocallsHasUser,srvVideocallsAsync);
            ws.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(ws.createSSLContext()));
            ws.start();
            return ws;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start WebSocketVideocall", e);
        }
    }
}
