package de.kaleidox.kram.cards;

import org.comroid.api.IntegerAttribute;

public class Deck extends Card.Hand {
    public final Type type;
    public final int multiplier;

    public Deck(Type type, int multiplier) {
        super(type.maxLength * multiplier);

        this.type = type;
        this.multiplier = multiplier;

        populateDeck();
    }

    private void populateDeck() {
        Card.Value[] values = Card.Value.values();
        for (int r = 0; r < multiplier; r++)
            for (Card.Face face : Card.Face.values())
                for (int v = type.minOrdinal; v < values.length; v++)
                    add(new Card(face, IntegerAttribute.valueOf(v, Card.Value.class)
                            .assertion("No Card value found with value " + v)));
    }

    public enum Type {
        full(0, Card.Face.values().length * Card.Value.values().length),
        from7(6, Card.Face.values().length * (Card.Value.values().length - 6));

        final int minOrdinal;
        final int maxLength;

        Type(int minOrdinal, int maxLength) {
            this.minOrdinal = minOrdinal;
            this.maxLength = maxLength;
        }
    }
}
