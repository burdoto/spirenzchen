package de.kaleidox.kram.cards;

public class Player {
    private final int number;
    public Card.Hand hand;

    public Player(int no) {
        this.number = no;
        this.hand = new Card.Hand(6);
    }

    @Override
    public String toString() {
        return "Player %d".formatted(number);
    }
}
