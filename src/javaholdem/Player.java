/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Player.java
 * Represents a player
 */

package javaholdem;

public final class Player {
    public String name;
    public float balance;

    // Creates a new player with the given name and balance
    public Player(String name, float start) {
        this.name = name;
        balance = start;
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f)", name, balance);
    }
}
