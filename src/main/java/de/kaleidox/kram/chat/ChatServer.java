package de.kaleidox.kram.chat;

import org.comroid.api.ContextualProvider;
import org.comroid.api.ResourceLoader;
import org.comroid.api.os.OS;
import org.comroid.restless.REST;
import org.comroid.restless.adapter.java.JavaHttpAdapter;
import org.comroid.uniform.Context;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.webkit.config.WebkitConfiguration;
import org.comroid.webkit.config.WebkitResourceLoader;
import org.comroid.webkit.model.PagePropertiesProvider;
import org.comroid.webkit.server.WebkitServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public final class ChatServer implements PagePropertiesProvider, ContextualProvider.Underlying {
    public static final int DEFAULT_PORT_BASE = 42420;
    public static final String URL_BASE = "http://comroid.org:" + DEFAULT_PORT_BASE;
    public static final ContextualProvider CONTEXT = FastJSONLib.fastJsonLib.plus("static ChatServer", new JavaHttpAdapter());
    public static ChatServer instance;
    private final ContextualProvider context;
    private final WebkitServer webkit;

    public InetAddress getAddress() {
        return webkit.getRest().getServer().getAddress().getAddress();
    }

    public int getPort() {
        return webkit.getRest().getServer().getAddress().getPort();
    }

    @Override
    public ContextualProvider getUnderlyingContextualProvider() {
        return context;
    }

    private ChatServer(InetAddress address, int portBase) throws IOException {
        this.context = CONTEXT.plus("ChatServer", this, ForkJoinPool.commonPool());
        this.webkit = new WebkitServer(
                this,
                ForkJoinPool.commonPool(),
                URL_BASE,
                address,
                portBase,
                portBase + 1,
                ChatConnection::new,
                this,
                ChatEndpoints.VALUES
        );
    }

    public static void main(String[] args) throws IOException {
        instance = new ChatServer(
                OS.isWindows
                        ? InetAddress.getLocalHost()
                        : InetAddress.getByAddress(new byte[]{0, 0, 0, 0}),
                DEFAULT_PORT_BASE
        );
    }

    @Override
    public Map<String, Object> findPageProperties(REST.Header.List headers) {
        return new HashMap<>();
    }
}
