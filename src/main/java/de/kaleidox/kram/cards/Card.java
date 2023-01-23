package de.kaleidox.kram.cards;

import org.comroid.api.IntegerAttribute;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Stack;

public class Card implements Comparable<Card> {
    public final Face face;
    public final Value value;

    public Card(Face face, Value value) {
        this.face = face;
        this.value = value;
    }

    @Override
    public int compareTo(@NotNull Card o) {
        return ((10 * (face.ordinal() - o.face.ordinal()))) + (value.ordinal() - o.value.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return face == card.face && value == card.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(face, value);
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} of {1}", value, face);
    }

    public static class Stack extends java.util.Stack<Card> {
        public final int maxSize;

        public Stack() {
            this(Integer.MAX_VALUE);
        }

        public Stack(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public boolean add(Card card) {
            if (size() + 1 > maxSize)
                throw new IllegalArgumentException("Too many elements");
            return super.add(card);
        }

        @Override
        public void add(int index, Card element) {
            if (size() + 1 > maxSize)
                throw new IllegalArgumentException("Too many elements");
            super.add(index, element);
        }

        public void fill(Deck fromDeck, int toSize) {
            if (size() != 0)
                throw new RuntimeException("Hand is not empty");

            Collections.shuffle(fromDeck);

            for (int i = 0; i < toSize; i++)
                add(fromDeck.remove(0));
        }
    }
    public enum Face implements Comparable<Face>, IntegerAttribute { Hearts, Diamonds, Clubs, Spades }
    public enum Value implements Comparable<Value>, IntegerAttribute {
        Val1, Val2, Val3, Val4, Val5, Val6, Val7, Val8, Val9, Jack, Queen, King, Ace;

        @Override
        public String toString() {
            return ordinal() < 9 ? String.valueOf(ordinal() + 1) : name();
        }
    }
}
