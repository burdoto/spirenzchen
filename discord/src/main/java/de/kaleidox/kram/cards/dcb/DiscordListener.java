package de.kaleidox.kram.cards.dcb;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.InteractPage;
import com.github.ygimenez.model.Page;
import de.kaleidox.kram.cards.Card;
import de.kaleidox.kram.cards.CardGame;
import de.kaleidox.kram.cards.GameType;
import de.kaleidox.kram.cards.dcb.entity.GamePlayer;
import de.kaleidox.kram.cards.dcb.entity.Room;
import de.kaleidox.kram.cards.dcb.repo.PlayerRepository;
import de.kaleidox.kram.cards.dcb.repo.RoomRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.comroid.api.BitmaskAttribute;
import org.comroid.util.Bitmask;
import org.comroid.util.StreamUtil;
import org.intellij.lang.annotations.MagicConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

@Component
public class DiscordListener extends ListenerAdapter {
    @Autowired
    private RoomRepository rooms;
    @Autowired
    private PlayerRepository players;
    @Lazy
    @Autowired
    private JDA jda;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        var userId = event.getUser().getIdLong();
        var room = rooms.findByPlayersUserId(userId).orElse(null);
        var player = players.findById(userId).orElseGet(() -> {
            var plr = new GamePlayer(userId);
            players.save(plr);
            return plr;
        });
        EmbedBuilder embed = new EmbedBuilder();
        switch (event.getName()) {
            case "create":
                room = new Room();
                room.open = !requireNonNullElse(event.getOption("private", OptionMapping::getAsBoolean), false);
                rooms.save(room);
                event.reply("Room %s created".formatted(room.code)).setEphemeral(true).queue();
                return;
            case "rooms":
                var pages = new ArrayList<Page>();
                var c = 0;
                for (var openRoom : rooms.findOpenRooms()) {
                    if (embed == null)
                        embed = embed.setTitle("Open Rooms");
                    embed.addField(
                            "Room `%s`".formatted(openRoom.code),
                            "%d players".formatted(openRoom.players.size()),
                            false
                    );
                    if (++c >= 10) {
                        pages.add(new InteractPage(embed.build()));
                        embed = null;
                        c = 0;
                    }
                }
                event.replyEmbeds((MessageEmbed) pages.get(0).getContent()).setEphemeral(true)
                        .queue(hook -> Pages.paginate(hook.retrieveOriginal().complete(), pages, true));
                return;
            case "join":
                var code = event.getOption("code", OptionMapping::getAsString);
                assert code != null;
                room = rooms.findById(code).orElse(null);
                if (room == null) {
                    event.reply("Room %s was not found".formatted(code)).setEphemeral(true).queue();
                    return;
                }
                room.players.add(player);
                event.reply("Room %s joined".formatted(room.code)).setEphemeral(true).queue();
                return;
            case "leave":
                if (isInvalid(event, room, Check.ROOM))
                    return;
                room.players.remove(player);
                event.reply("Room %s left".formatted(room.code)).setEphemeral(true).queue();
                return;
            case "players":
                if (isInvalid(event, room, Check.ROOM))
                    return;
                for (GamePlayer plr : room.players) {
                    var usr = jda.getUserById(player.userId);
                    assert usr != null;
                    embed.addField(
                            "%s#%s".formatted(usr.getName(), usr.getDiscriminator()),
                            "In Hand: %d".formatted(plr.size()),
                            false
                    );
                }
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            case "start":
                if (isInvalid(event, room, Check.ROOM))
                    return;
                var type = event.getOption("game", option -> GameType.valueOf(option.getAsString()));
                if (type == null) {
                    event.reply("Invalid game type. Available options are: "
                            + Arrays.toString(GameType.values())).queue();
                    return;
                }
                Collections.shuffle(room.players);
                for (int i = 1; i <= room.players.size(); i++)
                    room.players.get(i - 1).number = i;
                room.game = new CardGame(StreamUtil.voidPrintStream(), type, room.players);
                event.reply("Starting a new round of %s with %s"
                                .formatted(type, Arrays.toString(room.players.toArray()))).queue();
                return;
            case "pass":
                if (isInvalid(event, room, Check.ROOM, Check.GAME))
                    return;
                room.game.pass();
                event.reply("%s's turn!".formatted(player.asUser(jda).getAsMention())).queue();
                return;
            case "top":
                if (isInvalid(event, room, Check.ROOM, Check.GAME))
                    return;
                var target = requireNonNullElse(event.getOption("target", OptionMapping::getAsString), "table");
                int index = requireNonNullElse(event.getOption("index", OptionMapping::getAsInt), 0);
                int amount = Math.min(requireNonNullElse(event.getOption("amount", OptionMapping::getAsInt), 1), 25);
                List<? extends Card.Stack> stacks = switch (target) {
                    case "player" -> room.players;
                    case "table" -> List.of(room.game.table);
                    case "deck" -> List.of(room.game.deck);
                    default -> Collections.emptyList();
                };
                if (stacks.size() <= index && index == 0) {
                    event.reply("No cards in " + target).setEphemeral(true).queue();
                    return;
                }
                List<Card> list = stacks.get(index).stream().limit(amount).toList();
                for (int i = 0; i < list.size(); i++) {
                    Card card = list.get(i);
                    embed.addField("`%s` Card %d".formatted(card.getIdent(), i + 1), card.toString(), false);
                }
                event.replyEmbeds(embed.setTitle("Top %s Card%s in %s %d"
                        .formatted(amount == 1 ? "" : amount, amount == 1 ? "" : "s", target, index)).build())
                        .setEphemeral(true).queue();
                return;
            case "sort":
                if (isInvalid(event, room, Check.ROOM, Check.GAME))
                    return;
                target = requireNonNullElse(event.getOption("target", OptionMapping::getAsString), "hand");
                index = requireNonNullElse(event.getOption("index", OptionMapping::getAsInt), 0);
                stacks = switch (target) {
                    case "player" -> room.players;
                    case "table" -> List.of(room.game.table);
                    case "hand" -> {
                        index = 0;
                        yield List.of(player);
                    }
                    case "deck" -> {
                        index = 0;
                        yield List.of(room.game.deck);
                    }
                    default -> Collections.emptyList();
                };
                Collections.sort(stacks.get(index));
                event.reply("You have sorted %s %s".formatted(target, "deck".equals(target) ? "" : index)).queue();
                return;
            case "hand":
                if (isInvalid(event, room, Check.ROOM, Check.GAME))
                    return;
                list = player;
                for (int i = 0; i < list.size(); i++) {
                    Card card = list.get(i);
                    embed.addField("`%s` Card %d".formatted(card.getIdent(), i + 1), card.toString(), false);
                }
                event.replyEmbeds(embed.setTitle("Your Hand:").build()).setEphemeral(true).queue();
                return;
            case "table":
                if (isInvalid(event, room, Check.ROOM, Check.GAME))
                    return;
                index = requireNonNullElse(event.getOption("index", OptionMapping::getAsInt), 0);
                list = room.game.table[index];
                for (int i = 0; i < list.size(); i++) {
                    Card card = list.get(i);
                    embed.addField("`%s` Card %d".formatted(card.getIdent(), i + 1), card.toString(), false);
                }
                event.replyEmbeds(embed.setTitle("Cards on table:").build()).setEphemeral(true).queue();
                return;
            case "draw":
                if (isInvalid(event, room, Check.ROOM, Check.GAME))
                    return;
                target = requireNonNullElse(event.getOption("target", OptionMapping::getAsString), "table");
                index = requireNonNullElse(event.getOption("index", OptionMapping::getAsInt), 0);
                amount = Math.min(requireNonNullElse(event.getOption("amount", OptionMapping::getAsInt), 1), 25);
                stacks = switch (target) {
                    case "player" -> room.players;
                    case "table" -> List.of(room.game.table);
                    case "deck" -> List.of(room.game.deck);
                    default -> Collections.emptyList();
                };
                if (stacks.size() <= index && index == 0) {
                    event.reply("No cards in " + target).setEphemeral(true).queue();
                    return;
                }
                amount = player.draw(stacks.get(index), amount);
                event.replyEmbeds(embed.setTitle("Drawn %d card%s from %s %d".formatted(amount, amount == 1 ? "" : "s", target, index))
                        .build()).setEphemeral(true).queue();
                return;
            case "play":
                if (isInvalid(event, room, Check.ROOM, Check.GAME))
                    return;
                var idents = requireNonNull(event.getOption("card", OptionMapping::getAsString)).split(" ");
                target = requireNonNullElse(event.getOption("target", OptionMapping::getAsString), "hand");
                index = requireNonNullElse(event.getOption("index", OptionMapping::getAsInt), 0);
                stacks = (switch (target) {
                    case "player" -> room.players;
                    case "table" -> List.of(room.game.table);
                    case "deck" -> {
                        index = 0;
                        yield List.of(room.game.deck);
                    }
                    default -> Collections.emptyList();
                });
                if (stacks.size() <= index && index == 0) {
                    event.reply("No cards in " + target).setEphemeral(true).queue();
                    return;
                }
                final int[] i = {0};
                final EmbedBuilder[] $embed = {embed};
                amount = player.transfer(stacks.get(index), Arrays.stream(idents)
                        .map(ident -> Card.parse(ident).orElseGet(() -> player.get(Integer.parseInt(ident))))
                        .peek(card -> $embed[0].addField("`%s` Card %d".formatted(card.getIdent(), i[0]++), card.toString(), false))
                        .mapToInt(player::indexOf)
                        .toArray());
                event.replyEmbeds($embed[0].setTitle("Played %d Card%s to %s %s"
                                .formatted(amount, amount == 1 ? "" : "s", target, "deck".equals(target) ? "" : index)).build())
                        .setEphemeral(true).queue();
                return;
            //case "dash":
        }

        // conclude round
        if (room != null && room.game != null && !room.game.playing) {
            if (room.game.winner == null)
                event.reply("The game has concluded in a tie!").queue();
            else event.reply("%s has won the game!".formatted(
                    ((GamePlayer) room.game.winner).asUser(jda).getAsMention())).queue();
            room.game = null;
            for (GamePlayer gamePlayer : room.players)
                gamePlayer.number = 0;
        }
    }

    private boolean isInvalid(SlashCommandInteractionEvent event, Room room, Check... checks) {
        var mask = Bitmask.combine(checks);
        if (Bitmask.isFlagSet(mask, Check.ROOM) && room == null) {
            event.reply("You are not in a room").setEphemeral(true).queue();
            return true;
        }
        if (Bitmask.isFlagSet(mask, Check.GAME) && room.game == null) {
            event.reply("There is no game running").setEphemeral(true).queue();
            return true;
        }
        return false;
    }

    private enum Check implements BitmaskAttribute<Check> { NONE, ROOM, GAME }
}
