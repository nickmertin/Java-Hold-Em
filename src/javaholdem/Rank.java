/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Rank.java
 * Represents a rank of a playing card
 */

package javaholdem;

public enum Rank {
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE;

    @Override
    public String toString() {
        String s = super.toString();
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
