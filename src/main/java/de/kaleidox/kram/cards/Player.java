package de.kaleidox.kram.cards;

import org.comroid.api.Named;

import java.util.List;
import java.util.stream.IntStream;

public class Player extends Card.Stack implements Named {
    public final int number;

    public Player(int no) {
        this.number = no;
    }

    public int draw(List<Card> from, int amount) {
        return draw(from, IntStream.rangeClosed(0, Math.min(from.size(), amount) - 1));
    }

    public int draw(List<Card> from, IntStream indices) {
        return (int) indices
                .mapToObj(from::remove)
                .filter(this::add)
                .count();
    }

    public void play(CardGame game, int idx, Card.Stack target) {
        var card = get(idx);
        if (game.type.canPlay(game, this, card, target)) {
            target.push(remove(idx));
            System.out.printf("Played card %d (%s) to table %d%n", idx, card, target);
        } else System.err.printf("You cannot play an %s to table %d%n", card, target);
    }

    @Override
    public String toString() {
        return "Player %d".formatted(number);
    }
}
