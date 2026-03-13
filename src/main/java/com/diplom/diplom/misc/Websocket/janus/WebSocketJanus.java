package com.diplom.diplom.misc.Websocket.janus;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class WebSocketJanus extends WebSocketClient {
    private final Map<String, CompletableFuture<JSONObject>> queueTransactions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean connected=false;
    private static Logger logger = LoggerFactory.getLogger(WebSocketJanus.class.getName());

    public WebSocketJanus(String serverUri) throws URISyntaxException {
        super(
            new URI(serverUri),
            new Draft_6455(
                Collections.emptyList(),
                Collections.singletonList(new Protocol("janus-protocol"))
            )
        );
    }

    public CompletableFuture<JSONObject> sendRequest(JSONObject request) {
        String transaction = request.getString("transaction");
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        queueTransactions.put(transaction, future);
        this.send(request.toString());
        return future.orTimeout(5, TimeUnit.SECONDS);
    }

    public boolean isConnected() {
        return connected;
    }

    public void enableKeepAliveRequests(Long sessionId) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                JSONObject keepAlive = new JSONObject()
                        .put("janus", "keepalive")
                        .put("session_id", sessionId)
                        .put("transaction", UUID.randomUUID().toString());
                this.send(keepAlive.toString());
            } catch (Exception e) {
                logger.error("Failed to send keepalive {}: {}", sessionId, e.getMessage());
                scheduler.shutdownNow();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void disableKeepAliveRequests() {
        scheduler.shutdown();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("WebSocketJanus connected");
        connected=true;
    }

    @Override
    public void onMessage(String msg) {
        JSONObject response = new JSONObject(msg);
        if (response.has("transaction")) {
            String transaction = response.getString("transaction");
            CompletableFuture<JSONObject> future = queueTransactions.remove(transaction);
            if (future != null) {
                future.complete(response);
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean b) {
        logger.info("WebSocketJanus closed with code {}: {}", code, reason);
        connected=false;
    }

    @Override
    public void onError(Exception e) {
        logger.error("WebSocketJanus error: {}", e.getMessage());
        connected=false;
    }
}
