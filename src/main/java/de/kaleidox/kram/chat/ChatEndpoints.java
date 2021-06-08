package de.kaleidox.kram.chat;

import org.comroid.api.Polyfill;
import org.comroid.api.StreamSupplier;
import org.comroid.restless.CommonHeaderNames;
import org.comroid.restless.HTTPStatusCodes;
import org.comroid.restless.REST;
import org.comroid.restless.exception.RestEndpointException;
import org.comroid.restless.server.ServerEndpoint;
import org.comroid.uniform.Context;
import org.comroid.uniform.node.UniNode;
import org.intellij.lang.annotations.Language;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Pattern;

public enum ChatEndpoints implements ServerEndpoint.This {
    TTT_join("/ttt") {
        @Override
        public REST.Response executeGET(Context context, URI requestURI, REST.Request<UniNode> request, String[] urlParams) throws RestEndpointException {
            REST.Header.List headers = new REST.Header.List();
            headers.add(CommonHeaderNames.CACHE_CONTROL, "No-Cache");
            headers.add(CommonHeaderNames.REDIRECT_TARGET, "http://" + request.getHeaders().getFirst("Host") + "/main/ttt#" + UUID.randomUUID());
            return new REST.Response(HTTPStatusCodes.TEMPORARY_REDIRECT, headers);
        }
    };
    public static final StreamSupplier<ServerEndpoint> VALUES = StreamSupplier.of(values());
    private final String extension;
    private final String[] regExp;
    private final Pattern pattern;

    @Override
    public String getUrlBase() {
        return ChatServer.URL_BASE;
    }

    @Override
    public String getUrlExtension() {
        return extension;
    }

    @Override
    public String[] getRegExpGroups() {
        return regExp;
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    ChatEndpoints(String extension, @Language("RegExp") String... regExp) {
        this.extension = extension;
        this.regExp = regExp;
        this.pattern = buildUrlPattern();
    }
}
