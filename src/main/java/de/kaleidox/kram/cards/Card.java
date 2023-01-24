package de.kaleidox.kram.cards;

import org.comroid.api.IntegerAttribute;
import org.comroid.api.Named;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class Card implements Comparable<Card>, Named {
    public static final Pattern pattern = Pattern.compile("([\\dJQKA]0?)([HDCS])");
    public final Face face;
    public final Value value;

    public Card(Face face, Value value) {
        this.face = face;
        this.value = value;
    }

    public static Optional<Card> parse(String ident) {
        var matcher = pattern.matcher(ident.toUpperCase());
        if (!matcher.matches())
            return Optional.empty();
        return Optional.of(new Card(
                switch (matcher.group(2).charAt(0)) {
                    case 'H' -> Face.Hearts;
                    case 'D' -> Face.Diamonds;
                    case 'C' -> Face.Clubs;
                    case 'S' -> Face.Spades;
                    default -> throw new IllegalArgumentException("Unexpected value: " + matcher.group(1).charAt(0));
                },
                switch (matcher.group(1)) {
                    case "1" -> Value.Val1;
                    case "2" -> Value.Val2;
                    case "3" -> Value.Val3;
                    case "4" -> Value.Val4;
                    case "5" -> Value.Val5;
                    case "6" -> Value.Val6;
                    case "7" -> Value.Val7;
                    case "8" -> Value.Val8;
                    case "9" -> Value.Val9;
                    case "10" -> Value.Val10;
                    case "J" -> Value.Jack;
                    case "K" -> Value.King;
                    case "Q" -> Value.Queen;
                    case "A" -> Value.Ace;
                    default -> throw new IllegalStateException("Unexpected value: " + matcher.group(2).charAt(0));
                }));
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

    @Override
    public String getAlternateName() {
        String value = toString();
        return value + " ".repeat(Math.max(0, 5 - value.length()));
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

        public void transfer(Card.Stack from, int amount) {
            if (size() != 0)
                throw new RuntimeException("Hand is not empty");

            Collections.shuffle(from);

            for (int i = 0; i < amount; i++)
                add(from.remove(0));
        }
    }

    public enum Face implements Comparable<Face>, IntegerAttribute {Hearts, Diamonds, Clubs, Spades}

    public enum Value implements Comparable<Value>, IntegerAttribute {
        Val1, Val2, Val3, Val4, Val5, Val6, Val7, Val8, Val9, Val10, Jack, Queen, King, Ace;

        @Override
        public String toString() {
            return ordinal() < 10 ? String.valueOf(ordinal() + 1) : name();
        }
    }
}
