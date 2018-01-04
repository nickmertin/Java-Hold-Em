/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Card.java
 * Represents a suit of a playing card
 */

package javaholdem;

public enum Suit {
    HEARTS,
    DIAMONDS,
    SPADES,
    CLUBS;

    @Override
    public String toString() {
        String s = super.toString();
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
