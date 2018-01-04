/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Hand.java
 * Represents a hand (round) in the game
 */

package javaholdem;

public final class Hand {
    public PlayerInHand[] players;
    public float mainPotBalance;
    public Sidepot[] sidepots;
    public Card[] communityCards;
    public float lastBet, maxBet, required;
    public int player, stage;
    public Player lastToBet;
    public boolean firstTurn;

    // Creates a new hand
    Hand(PlayerInHand[] players, float startBalance, float lastBet, int player) {
        this.players = players;
        mainPotBalance = startBalance;
        this.lastBet = lastBet;
        sidepots = new Sidepot[0];
        communityCards = new Card[0];
        required = 0;
        this.player = player;
        lastToBet = players[player].player;
        stage = 0;
        maxBet = Float.POSITIVE_INFINITY;
        firstTurn = true;
    }
}
