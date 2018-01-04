/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Card.java
 * Represents a sidepot within a hand
 */

package javaholdem;

import java.util.Arrays;

public final class Sidepot {
    public PlayerInHand[] players;
    public float balance, bet;

    // Creates a new sidepot with the given players and bet from each player
    public Sidepot(PlayerInHand[] players, float bet) {
        this.players = players;
        this.bet = bet;
        balance = 0;
    }

    @Override
    public String toString() {
        return String.format("%s: $%.2f ($%.2f ea.)", Arrays.stream(players).map(p -> p.player.name).reduce((x, y) -> x + ", " + y).get(), balance, bet);
    }
}
