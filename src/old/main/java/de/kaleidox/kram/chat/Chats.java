package de.kaleidox.kram.chat;

import org.comroid.mutatio.ref.ReferenceMap;

import java.util.UUID;

public final class Chats {
    public static final Chats instance = new Chats();
    private final ReferenceMap<UUID, ChatConnection> connections;

    private Chats() {
        this.connections = new ReferenceMap<>();
    }

    public static boolean push(ChatConnection connection, String message) {
        return broadcast(String.format("[%s] %s", connection.getUUID(), message));
    }

    private static boolean broadcast(final String text) {
        instance.connections.values().forEach(conn -> conn.sendCommand("chat/receive", text));
        return true;
    }

    public static boolean pushConnection(ChatConnection connection) {
        if (instance.connections.put(connection.getUUID(), connection) != connection)
            return push(connection, "Joined the Channel");
        return false;
    }
}
