package de.kaleidox.kram.chat;

import de.kaleidox.kram.ttt.TicTacToe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger();
    private final UUID uuid = UUID.randomUUID();

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public ChatConnection(WebSocket socketBase, REST.Header.List headers, ContextualProvider context) {
        super(socketBase, headers, context);
/* temporarily ttt connection
        if (!Chats.pushConnection(this))
            throw new RuntimeException("Could not join Chat");
 */
    }

    void appendMessage(String text) {
        sendCommand("chat/receive", text);
    }

    @Override
    protected void handleCommand(Map<String, Object> pageProperties, String category, String name, UniNode data, UniObjectNode response) {
        if (category.equals("ttt")) {
            handleTTTCommand(pageProperties, name, data, response);
            return;
        }

        if (!category.equals("message"))
            return;

        if (!name.equals("send"))
            return;
        String message = data.asString();

        if (!Chats.push(this, message))
            sendToPanel("error");
    }

    private void handleTTTCommand(Map<String, Object> pageProperties, String name, UniNode data, UniObjectNode response) {
        switch (name) {
            case "connect":
                TicTacToe game = TicTacToe.connectGame(data.use("session")
                        .map(UniNode::asString)
                        .ifPresentMap(UUID::fromString), this);
                if (game != null)
                    sendCommand("ttt/start");
                break;
            case "callBox":
                TicTacToe.callBox(this, data.get("index").asInt());
                break;
            default:
                logger.warn("Unknown TTT command received: " + name);
        }
    }
}
