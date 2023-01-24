package de.kaleidox.kram.cards;

import org.comroid.api.IntegerAttribute;

import java.util.Arrays;
import java.util.Vector;

public enum GameType implements IntegerAttribute {
    MauMau(Deck.Preset.from7, 2) {
        @Override
        public Card.Stack[] init(CardGame cardGame) {
            Card.Stack[] stacks = super.init(cardGame);
            stacks[0].transfer(cardGame.deck, 1);
            return stacks;
        }

        @Override
        public void handleLine(CardGame game, String line, String[] cmds) {
            if (cmds.length == 1) {
                switch (cmds[0]) {
                    case "mau":
                        System.out.println("mau");
                }
            } else {
                System.out.println("Invalid command: " + line);
            }
        }

        @Override
        public boolean conclude(CardGame game) {
            return (game.winner = Arrays.stream(game.players)
                    .filter(Vector::isEmpty)
                    .findAny()
                    .orElse(null)) != null;
        }

        @Override
        public boolean canPlay(CardGame game, Player player, Card card, Card.Stack table) {
            if (table.empty())
                return false;
            var top = table.peek();
            return top.face == card.face ||
                      (top.value.ordinal() > Card.Value.Val10.ordinal()
                    && top.value == card.value);
        }

        @Override
        public void cardPlayed(CardGame game, Card card) {
            switch (card.value) {
                case Val7 -> {
                    game.getNextPlayer().draw(game.deck, 2);
                    System.out.printf("%s must draw 2 cards%n", game.getNextPlayer());
                }
                case Val8 -> {
                    game.$advanceIndex();
                    System.out.printf("%s is skipped%n", game.nextPlayer());
                }
                case Val9 -> {
                    game.currentPlayerAdvancer = game.currentPlayerAdvancer.andThen(x -> x * -1);
                    System.out.printf("The playing direction has changed%n");
                }
            }
        }
    },
    Poker(4, Deck.Preset.full, 2) {
    };

    public final int decks;
    public final Deck.Preset deckPreset;
    public final int starterCards;

    GameType(Deck.Preset deckPreset, int starterCards) {
        this(1, deckPreset, starterCards);
    }

    GameType(int decks, Deck.Preset deckPreset, int starterCards) {
        this.decks = decks;
        this.deckPreset = deckPreset;
        this.starterCards = starterCards;
    }

    public Card.Stack[] init(CardGame cardGame) {
        return new Card.Stack[]{new Card.Stack()};
    }

    public void handleLine(CardGame game, String line, String[] cmds) {
    }

    /**
     * @param game Game
     * @return game over
     */
    public boolean conclude(CardGame game) {
        return false;
    }

    public boolean canPlay(CardGame game, Player player, Card card, Card.Stack table) {
        return true;
    }

    public void cardPlayed(CardGame game, Card item) {
    }
}
