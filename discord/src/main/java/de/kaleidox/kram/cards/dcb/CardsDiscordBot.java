package de.kaleidox.kram.cards.dcb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ygimenez.model.Paginator;
import com.github.ygimenez.model.PaginatorBuilder;
import de.kaleidox.kram.cards.dcb.entity.Room;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.comroid.api.io.FileHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.io.IOException;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan(basePackageClasses = Room.class)
public class CardsDiscordBot extends ListenerAdapter {
    public final static FileHandle DIR = new FileHandle("/srv/dcb/cards/", true);
    public final static FileHandle DB_FILE = DIR.createSubFile("db.json");
    public final static FileHandle BOT_TOKEN = DIR.createSubFile("bot.token");

    public static void main(String[] args) {
        SpringApplication.run(CardsDiscordBot.class);
    }

    @Bean
    public DataSource dataSource(@Autowired ObjectMapper objectMapper) throws IOException {
        var dbInfo = objectMapper.readValue(DB_FILE.openReader(), DBInfo.class);
        return DataSourceBuilder.create()
                .url(dbInfo.url)
                .username(dbInfo.username)
                .password(dbInfo.password)
                .build();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JDA jda(@Autowired DiscordListener listener) {
        JDA jda = JDABuilder.createLight(BOT_TOKEN.getContent(true))
                .addEventListeners(listener)
                .build();
        jda.updateCommands().addCommands(
                // rooms
                Commands.slash("create", "Create a room")
                        .addOption(OptionType.BOOLEAN, "private", "Whether the room is private [true]"),
                Commands.slash("rooms", "List all public rooms"),
                Commands.slash("join", "Join a room")
                        .addOption(OptionType.STRING, "code", "Room Code", true),
                Commands.slash("leave", "Leave the room"),
                Commands.slash("players", "List players in the room"),
                Commands.slash("start", "Starts a new game")
                        .addOption(OptionType.STRING, "game", "The type of game to play [MauMau]"),

                // game
                Commands.slash("pass", "Pass the round"),
                Commands.slash("top", "Shows the top card of a stack")
                        .addOption(OptionType.STRING, "target", "The target stack [table]")
                        .addOption(OptionType.INTEGER, "index", "The index of the target to use [0]")
                        .addOption(OptionType.INTEGER, "amount", "The amount to read from top [1]"),
                Commands.slash("sort", "Shows the top card of a stack")
                        .addOption(OptionType.STRING, "target", "The target stack [hand]")
                        .addOption(OptionType.INTEGER, "index", "The index of the stack [0]"),
                Commands.slash("hand", "Shows your current hand"),
                Commands.slash("table", "Shows the top card on a stack on the table")
                        .addOption(OptionType.INTEGER, "index", "The index of the stack [0]"),
                Commands.slash("draw", "Draw cards from a target")
                        .addOption(OptionType.STRING, "target", "Where to draw from [deck]")
                        .addOption(OptionType.INTEGER, "index", "Index of the target to use [0]")
                        .addOption(OptionType.INTEGER, "amount", "How many cards to draw [1]"),
                Commands.slash("play", "Play a card")
                        .addOption(OptionType.STRING, "card", "Card indices and identifiers, space separated", true)
                        .addOption(OptionType.STRING, "target", "Where to play to [table]")
                        .addOption(OptionType.INTEGER, "index", "Index of the target to use [0]")
                //Commands.slash("dash", "Shows your current hand and the table")
        ).queue();
        return jda;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Paginator paginator(@Autowired JDA jda) {
        return PaginatorBuilder.createSimplePaginator(jda);
    }

    private static class DBInfo {
        public String url;
        public String username;
        public String password;

    }
}
