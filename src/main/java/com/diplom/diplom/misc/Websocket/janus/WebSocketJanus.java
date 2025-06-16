package com.diplom.diplom.misc.Websocket.janus;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.json.JSONObject;

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
    private final List<Consumer<JSONObject>> eventListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean connected=false;

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

    public CompletableFuture<JSONObject> waitForEvent(long sessionId, long handleId) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        Consumer<JSONObject> listener = new Consumer<>() {
            @Override
            public void accept(JSONObject event) {
                if ("event".equals(event.optString("janus"))
                        && event.optLong("session_id") == sessionId
                        && event.optLong("sender") == handleId) {
                    eventListeners.remove(this);
                    future.complete(event);
                }
            }
        };
        eventListeners.add(listener);
        return future.orTimeout(15, TimeUnit.SECONDS);
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
                System.err.println("Failed to send keepalive "+sessionId+": " + e.getMessage());
                scheduler.shutdownNow();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void disableKeepAliveRequests() {
        scheduler.shutdown();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("WebSocketJanus connected");
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
        for (Consumer<JSONObject> listener : eventListeners) {
            listener.accept(response);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean b) {
        System.out.println("WebSocketJanus closed with code "+code+": " + reason);
        connected=false;
    }

    @Override
    public void onError(Exception e) {
        System.err.println("WebSocketJanus error: " + e.getMessage());
        connected=false;
    }
}
