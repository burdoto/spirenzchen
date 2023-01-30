package de.kaleidox.kram.chat;

import org.comroid.api.ContextualProvider;
import org.comroid.api.os.OS;
import org.comroid.restless.REST;
import org.comroid.webkit.model.PagePropertiesProvider;
import org.comroid.webkit.server.WebkitServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public final class ChatServer implements PagePropertiesProvider, ContextualProvider.Underlying {
    public static final int DEFAULT_PORT_BASE = 42420;
    public static final String URL_BASE = "http://comroid.org:" + DEFAULT_PORT_BASE;
    public static final ContextualProvider CONTEXT = ContextualProvider.getRoot();
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
        Random rng = new Random();
        IntStream.generate(rng::nextInt)
                .limit(20)
                .map(x -> x % 10)
                .mapToObj(x -> {
                    switch (x) {
                        case 0:
                            return "zero=0";
                        case 1:
                            return "one=1";
                        case 2:
                            return "two=2";
                        case 3:
                            return "three=3";
                        case 4:
                            return "four=4";
                        case 5:
                            return "five=5";
                        case 6:
                            return "six=6";
                        case 7:
                            return "seven=7";
                        case 8:
                            return "eight=8";
                        case 9:
                            return "nine=9";
                        default:
                            return "kp=-1";
                    }
                })
                .map(str -> str.split("=")[1])
                .mapToInt(Integer::parseInt)
                .filter(x -> x < 5)
                .forEach(System.out::println);


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
