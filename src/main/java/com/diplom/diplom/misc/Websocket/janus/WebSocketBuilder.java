package com.diplom.diplom.misc.Websocket.janus;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WebSocketBuilder {
    private URI uri;
    private BiConsumer<WebSocketClient,String> onMessage;
    private BiConsumer<Integer, String> onClose;
    private Consumer<Exception> onError;
    private BiConsumer<WebSocketClient, ServerHandshake> onOpenHandler;

    public WebSocketBuilder uri(String uri) {
        this.uri = URI.create(uri);
        return this;
    }

    public WebSocketBuilder onOpen(BiConsumer<WebSocketClient, ServerHandshake> handler) {
        this.onOpenHandler = handler;
        return this;
    }

    public WebSocketBuilder onMessage(BiConsumer<WebSocketClient,String> handler) {
        this.onMessage = handler;
        return this;
    }

    public WebSocketBuilder onClose(BiConsumer<Integer, String> handler) {
        this.onClose = handler;
        return this;
    }

    public WebSocketBuilder onError(Consumer<Exception> handler) {
        this.onError = handler;
        return this;
    }

    public WebSocketClient build() {
        return new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                if (onOpenHandler != null) onOpenHandler.accept(this,handshakedata);
            }

            @Override
            public void onMessage(String message) {
                if (onMessage != null) onMessage.accept(this,message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (onClose != null) onClose.accept(code, reason);
            }

            @Override
            public void onError(Exception ex) {
                if (onError != null) onError.accept(ex);
            }
        };
    }
}
