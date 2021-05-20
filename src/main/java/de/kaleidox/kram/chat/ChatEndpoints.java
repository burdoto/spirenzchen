package de.kaleidox.kram.chat;

import org.comroid.api.StreamSupplier;
import org.comroid.restless.server.ServerEndpoint;
import org.intellij.lang.annotations.Language;

import java.util.regex.Pattern;

public enum ChatEndpoints implements ServerEndpoint.This {
    ;
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
