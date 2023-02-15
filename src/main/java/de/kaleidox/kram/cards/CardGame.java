package de.kaleidox.kram.cards;

import org.comroid.api.Polyfill;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CardGame {
    private static final BufferedReader in;

    static {
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    public final PrintStream out;
    public final GameType type;
    public final List<Player> players;
    public final Deck deck;
    public Card.Stack[] table;
    int currentPlayer;
    IntUnaryOperator currentPlayerAdvancer = x -> x + 1;
    public boolean playing = true;
    @Nullable
    public Player winner;

    public GameType getType() {
        return type;
    }

    public List<? extends Player> getPlayers() {
        return players;
    }

    public Deck getDeck() {
        return deck;
    }

    public Card.Stack[] getTable() {
        return table;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayer);
    }
    public Player getNextPlayer() {
        return players.get(advancePlayerIndex());
    }

    public @Nullable Player getWinner() {
        return winner;
    }

    public CardGame(PrintStream out, GameType type, List<? extends Player> players) {
        this.out = out;
        this.type = type;
        this.players = (List<Player>) players;
        this.deck = new Deck(type.deckPreset, type.decks);
        this.table = type.init(this);

        // init players
        for (Player plr : this.players) {
            plr.draw(deck, type.starterCards);
            plr.sort(Card::compareTo);
        }

        Collections.shuffle(deck);
    }

    public CardGame(PrintStream out, GameType type, int players) {
        this(out, type, IntStream.rangeClosed(1, players)
                .mapToObj(Player::new)
                .collect(Collectors.toList()));
    }

    public Player addPlayer() {
        var plr = new Player(players.size());
        players.add(Polyfill.uncheckedCast(plr));
        return plr;
    }

    public static void main(String[] args) {
        if (args.length > 0)
            new CardGame(System.out, GameType.valueOf(args[0]), Integer.parseInt(args[1])).play();
        else while (true) {
            try (Closeable unused = in) {
                int players = 2;
                CardGame game;
                System.out.print("game> ");
                var line = in.readLine();

                if (line.equals("exit"))
                    return;

                try {
                    game = new CardGame(System.out, GameType.valueOf(line), players);
                } catch (IllegalArgumentException e) {
                    System.out.println("error: " + e.getMessage());
                    continue;
                }

                game.play();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public Player nextPlayer() {
        return players.get(advancePlayerIndex());
    }

    void $advanceIndex() {
        currentPlayer = advancePlayerIndex();
    }

    private int advancePlayerIndex() {
        int i = currentPlayerAdvancer.applyAsInt(currentPlayer);
        i = currentPlayer
                = i >= players.size()
                ? i % players.size()
                : i < 0
                ? i + players.size()
                : i;
        return i;
    }

    public void pass() {
        out.printf("%s's turn%n", nextPlayer());
        playing = !type.conclude(this);
    }

    private void play() {
        while (playing) {
            try {
                handleCmds("dash");
                out.print(type.name() + "> ");
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
            out.printf("Game concluded as a tie!%n");
        else out.printf("%s has won the game!%n", winner);
    }

    void handleCmds(String... cmds) {
        var player = getCurrentPlayer();
        player.sort(Card::compareTo);
        switch (cmds[0]) {
            case "exit":
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
                from = (table ? this.table[idx] : players.get(Math.max(0, idx - 1)));
                out.printf("Top card in %s: %s%n",
                        table ? "Table " + idx : from,
                        from.empty() ? "nothing" : from.peek());
                break;
            case "hand":
                List<Card> hand = players.get(cmds.length == 2
                        ? Math.max(0, Integer.parseInt(cmds[1]) - 1)
                        : currentPlayer);
                hand.sort(Card::compareTo);
                for (int i = 0; i < hand.size(); i++)
                    out.printf("%d\t- %s%n", i, hand.get(i).getAlternateName());
                break;
            case "players":
                for (Player plr : players)
                    out.println(plr);
                break;
            case "deck":
                for (Card card : deck)
                    out.println(card);
                break;
            case "draw":
                int drawn = player.draw(
                        from = cmds.length == 3
                                ? players.get(Math.max(0, Integer.parseInt(cmds[2]) - 1))
                                : deck,
                        cmds.length == 2
                                ? Integer.parseInt(cmds[1])
                                : 1);
                out.printf("Drawn %d cards from %s%n", drawn, from.toString());
                if (drawn == 1)
                    handleCmds("top");
                pass();
                break;
            case "play":
                Optional<Card> parse = Card.parse(cmds[1]);
                idx = parse.map(card -> getCurrentPlayer().indexOf(card))
                        .orElseGet(() -> Integer.parseInt(cmds[1]));
                if (idx == -1)
                {
                    out.printf("Card not found in Player %d: %s%n", currentPlayer + 1, parse.orElse(null));
                    break;
                }
                var tgt = cmds.length >= 3 ? Integer.parseInt(cmds[2]) : 0;
                if (player.play(this, idx, this.table[tgt]))
                    pass();
                break;
            case "dash":
                // clear console?
                out.println(getCurrentPlayer());
                handleCmds("hand");
                handleCmds("table");
            default:
                type.handleLine(this, String.join(" ", cmds), cmds);
        }
    }
}
