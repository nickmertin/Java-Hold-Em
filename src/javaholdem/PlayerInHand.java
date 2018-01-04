/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * PlayerInHand.java
 * Represents a moniker to a player within a hand
 */

package javaholdem;

public final class PlayerInHand {
    public Player player;
    public Card card1, card2;
    public float bet;

    // Creates a new player moniker
    public PlayerInHand(Player player, Card card1, Card card2) {
        this.player = player;
        this.card1 = card1;
        this.card2 = card2;
        bet = 0;
    }
}
