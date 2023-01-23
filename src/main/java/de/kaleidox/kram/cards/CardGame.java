package de.kaleidox.kram.cards;

import java.io.*;
import java.util.stream.IntStream;

public class CardGame {
    public final GameType gameType;
    public final Player[] players;
    public final Deck deck;
    private int currentPlayer;

    public Player nextPlayer() {
        var plr = currentPlayer = currentPlayer + 1 > players.length ? 0 : currentPlayer + 1;
        return players[plr];
    }

    public Player getCurrentPlayer() {
        return players[currentPlayer];
    }

    public CardGame(GameType gameType, int players) {
        this.gameType = gameType;
        this.players = IntStream.rangeClosed(1, players)
                .mapToObj(Player::new)
                .toArray(Player[]::new);
        this.deck = new Deck(gameType.deckType, gameType.decks);

        // init players
        for (Player plr : this.players) {
            plr.hand.fill(deck);
            plr.hand.sort(Card::compareTo);
        }

        deck.sort(Card::compareTo);
    }

    private void play() {
        try (InputStreamReader isr = new InputStreamReader(System.in);
             BufferedReader in = new BufferedReader(isr);
             PrintStream out = System.out
        ) {
            while (true) {
                out.print(gameType.name() + "> ");
                var line = in.readLine();

                gameType.handleLine(this, line);

                if (line.equals("exit"))
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (InputStreamReader isr = new InputStreamReader(System.in);
             BufferedReader in = new BufferedReader(isr);
             PrintStream out = System.out
        ) {
            int players = 2;
            CardGame game;
            while (true) {
                out.print("game> ");
                var line = in.readLine();

                if (line.equals("exit"))
                    return;

                try {
                    game = new CardGame(GameType.valueOf(line), players);
                } catch (IllegalArgumentException e) {
                    out.println("error: ");
                    e.printStackTrace(out);
                    return;
                }

                game.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
