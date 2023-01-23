package de.kaleidox.kram.cards;

import org.comroid.api.IntegerAttribute;

public enum GameType implements IntegerAttribute {
    MauMau(Deck.Preset.from7, 6) {
        @Override
        public Card.Stack[] init(CardGame cardGame) {
            Card.Stack[] stacks = super.init(cardGame);
            stacks[0].fill(cardGame.deck, 1);
            return stacks;
        }

        @Override
        public void handleLine(CardGame game, String line, String[] cmds) {
            if (cmds.length == 1) {
                if (cmds[0].equals("mau")) {
                    System.out.println("mau");
                    return;
                }
                throw new IllegalStateException("Unexpected value: " + cmds[0]);
            } else {
                System.out.println("Invalid command: " + line);
            }
        }

        @Override
        public boolean canPlay(CardGame game, Player player, Card card, Card.Stack table) {
            if (table.empty())
                return false;
            var top = table.peek();
            return top.face == card.face || top.value == card.value;
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

    public boolean canPlay(CardGame game, Player player, Card card, Card.Stack table) {
        return true;
    }
}