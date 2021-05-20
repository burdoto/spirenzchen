package de.kaleidox.kram.chat;

import org.comroid.api.ContextualProvider;
import org.comroid.api.UUIDContainer;
import org.comroid.restless.REST;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.webkit.socket.WebkitConnection;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.UUID;

public final class ChatConnection extends WebkitConnection implements UUIDContainer {
    private final UUID uuid = UUID.randomUUID();

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public ChatConnection(WebSocket socketBase, REST.Header.List headers, ContextualProvider context) {
        super(socketBase, headers, context);

        if (!Chats.pushConnection(this))
            throw new RuntimeException("Could not join Chat");
    }

    void appendMessage(String text) {
        sendCommand("chat/receive", text);
    }

    @Override
    protected void handleCommand(Map<String, Object> pageProperties, String category, String name, UniNode data, UniObjectNode response) {
        if (!category.equals("message"))
            return;

        if (!name.equals("send"))
            return;
        String message = data.asString();

        if (!Chats.push(this, message))
            sendToPanel("error");
    }
}
