/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Card.java
 * Represents a playing card
 */

package javaholdem;

public final class Card {
    public Suit suit;
    public Rank rank;

    // Creates a new card with the given suit and rank
    Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    public String toString() {
        return String.format("%s of %s", rank.toString(), suit.toString());
    }
}
