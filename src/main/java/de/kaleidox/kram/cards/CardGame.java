package de.kaleidox.kram.cards;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

public class CardGame {
    private static final BufferedReader in;

    static {
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    public final GameType type;
    public final Player[] players;
    public final Deck deck;
    public Card.Stack[] table;
    private int currentPlayer;
    private boolean playing = true;
    @Nullable Player winner;

    public Player getCurrentPlayer() {
        return players[currentPlayer];
    }

    public CardGame(GameType type, int players) {
        this.type = type;
        this.players = IntStream.rangeClosed(1, players)
                .mapToObj(Player::new)
                .toArray(Player[]::new);
        this.deck = new Deck(type.deckPreset, type.decks);
        this.table = type.init(this);

        // init players
        for (Player plr : this.players) {
            plr.transfer(deck, type.starterCards);
            plr.sort(Card::compareTo);
        }

        Collections.shuffle(deck);
    }

    public static void main(String[] args) {
        while (true) {
            try (Closeable unused = in) {
                int players = 2;
                CardGame game;
                System.out.print("game> ");
                var line = in.readLine();

                if (line.equals("exit"))
                    return;

                try {
                    game = new CardGame(GameType.valueOf(line), players);
                } catch (IllegalArgumentException e) {
                    System.out.println("error: ");
                    e.printStackTrace();
                    return;
                }

                game.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Player nextPlayer() {
        var plr = currentPlayer = currentPlayer + 1 >= players.length ? 0 : currentPlayer + 1;
        return players[plr];
    }

    private void pass() {
        System.out.printf("%s's turn%n", nextPlayer());
        playing = !type.conclude(this);
    }

    private void play() {
        while (playing) {
            try {
                System.out.print(type.name() + "> ");
                var line = in.readLine();

                if (line.isEmpty())
                    continue;
                var cmds = line.split(" ");
                handleCmds(cmds);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (winner == null)
            System.out.printf("Game concluded as a tie!%n");
        else System.out.printf("%s has won the game!%n", winner);
    }

    private void handleCmds(String... cmds) {
        var player = getCurrentPlayer();
        player.sort(Card::compareTo);
        switch (cmds[0]) {
            case "exit":
                return;
            case "dash":
                handleCmds("hand");
                handleCmds("table");
                return;
            case "pass":
                pass();
                break;
            case "table":
            case "top":
                // top table 1 <-- list top card of table 1
                // table 1 <-- list also top card of table 1
                // top 1 <-- list top card of player 1
                Stack<Card> from;
                var iter = Arrays.stream(cmds).iterator();
                boolean table = iter.next().equals("table");
                int idx = 0;
                var arg0 = !iter.hasNext() ? null : iter.next();
                if ("table".equals(arg0))
                    table = true;
                var arg1 = !iter.hasNext() ? null : iter.next();
                if ((table && arg0 != null && arg0.matches("\\d+"))
                        || (arg1 != null && arg1.matches("\\d+")))
                    idx = Integer.parseInt(arg1 != null ? arg1 : arg0);
                from = (table ? this.table[idx] : players[Math.max(0, idx - 1)]);
                System.out.printf("Top card in %s: %s%n",
                        table ? "Table " + idx : from,
                        from.empty() ? "nothing" : from.peek());
                break;
            case "players":
                for (Player plr : players)
                    System.out.println(plr);
                break;
            case "deck":
                for (Card card : deck)
                    System.out.println(card);
                break;
            case "draw":
                int drawn = player.draw(
                        from = cmds.length == 3
                                ? players[Math.max(0, Integer.parseInt(cmds[2]) - 1)]
                                : deck,
                        cmds.length == 2
                                ? Integer.parseInt(cmds[1])
                                : 1);
                System.out.printf("Drawn %d cards from %s%n", drawn, from.toString());
                if (drawn == 1)
                    handleCmds("top");
                pass();
                break;
            case "play":
                idx = cmds.length >= 2 ? Integer.parseInt(cmds[1]) : 0;
                var tgt = cmds.length >= 3 ? Integer.parseInt(cmds[2]) : 0;
                player.play(this, idx, this.table[tgt]);
                pass();
                break;
            case "hand":
                List<Card> hand = players[cmds.length == 2
                        ? Math.max(0, Integer.parseInt(cmds[1]) - 1)
                        : currentPlayer];
                hand.sort(Card::compareTo);
                for (int i = 0; i < hand.size(); i++)
                    System.out.printf("%d\t- %s%n", i, hand.get(i));
                break;
            default:
                type.handleLine(this, String.join(" ", cmds), cmds);
        }
    }
}
