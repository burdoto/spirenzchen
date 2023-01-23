package de.kaleidox.kram.cards;

import org.comroid.api.IntegerAttribute;

public enum GameType implements IntegerAttribute {
    MauMau(Deck.Type.from7) {
        @Override
        public void handleLine(CardGame game, String line) {
            System.out.println("todo: implement maumau"); // todo
        }
    },
    Poker(4, Deck.Type.full) {
        @Override
        public void handleLine(CardGame game, String line) {
            System.out.println("todo: implement poker"); // todo
        }
    };

    public final int decks;
    public final Deck.Type deckType;

    GameType(Deck.Type deckType) {
        this(1, deckType);
    }

    GameType(int decks, Deck.Type deckType) {
        this.decks = decks;
        this.deckType = deckType;
    }

    public void handleLine(CardGame game, String line) {
        throw new AbstractMethodError();
    }
}
