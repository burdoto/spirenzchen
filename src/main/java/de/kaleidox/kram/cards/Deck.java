package de.kaleidox.kram.cards;

import org.comroid.api.IntegerAttribute;
import org.comroid.api.Named;

public class Deck extends Card.Stack implements Named {
    public final Preset preset;
    public final int multiplier;

    public Deck() {
        this(Preset.full, 1);
    }

    public Deck(Preset preset, int multiplier) {
        super(preset.maxLength * multiplier);

        this.preset = preset;
        this.multiplier = multiplier;

        populateDeck();
    }

    private void populateDeck() {
        Card.Value[] values = Card.Value.values();
        for (int r = 0; r < multiplier; r++)
            for (Card.Face face : Card.Face.values())
                for (int v = preset.minOrdinal; v < values.length; v++)
                    add(new Card(face, IntegerAttribute.valueOf(v, Card.Value.class)
                            .assertion("No Card value found with value " + v)));
    }

    public enum Preset {
        full(0, Card.Face.values().length * Card.Value.values().length),
        from7(6, Card.Face.values().length * (Card.Value.values().length - 6));

        final int minOrdinal;
        final int maxLength;

        Preset(int minOrdinal, int maxLength) {
            this.minOrdinal = minOrdinal;
            this.maxLength = maxLength;
        }
    }

    @Override
    public synchronized String toString() {
        return "Deck";
    }

    @Override
    public String getAlternateName() {
        return "Deck (%s*%d)".formatted(this.preset, this.multiplier);
    }
}
