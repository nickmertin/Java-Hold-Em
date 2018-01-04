/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * PlayerContext.java
 * Represents a player within their hand
 */

package javaholdem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class PlayerContext {
    public Game game;
    public PlayerInHand player;
    public float maxBet;

    // Creates a new context from the given game and player moniker
    PlayerContext(Game game, PlayerInHand player) {
        this.game = game;
        this.player = player;
        maxBet = Arrays.stream(game.current.players).filter(p -> p != player).map(p -> p.player.balance - p.bet + game.current.required).max(Float::compare).get();
    }

    // Folds the player from the current hand
    public void fold() {
        if (game.current.lastToBet == player.player)
            if (game.current.players[game.current.players.length - 1] == player)
                game.current.lastToBet = game.current.players[0].player;
            else
                for (int i = 0; i < game.current.players.length; i++)
                    if (game.current.players[i] == player) {
                        game.current.lastToBet = game.current.players[i + 1].player;
                        break;
                    }
        game.current.firstTurn = true;
        game.current.players = remove(game.current.players, player);
        for (Sidepot sidepot : game.current.sidepots)
            sidepot.players = remove(sidepot.players, player);
        --game.current.player;
    }

    // Calls or checks the player within the current hand
    public void callOrCheck() {
        float amount = game.current.required - player.bet;
        if (amount == 0)
            return;
        if (amount > player.player.balance) {
            // Going all-in
            amount = player.player.balance;
            player.player.balance = 0;
        }
        else
            player.player.balance -= amount;
        player.bet += amount;
        if (amount <= game.current.maxBet)
            game.current.mainPotBalance += amount;
        else {
            game.current.mainPotBalance += game.current.maxBet;
            float left = amount - game.current.maxBet;
            for (Sidepot sidepot : game.current.sidepots)
                if (left <= sidepot.bet) {
                    sidepot.balance += left;
                    break;
                }
                else {
                    sidepot.balance += sidepot.bet;
                    left -= sidepot.bet;
                }
        }
    }

    // Bets or raises on behalf of the player within the current hand
    public void betOrRaise(float bet) {
        game.current.required += bet;
        PlayerInHand[] oldPlayers = game.current.sidepots.length == 0 ? game.current.players : game.current.sidepots[game.current.sidepots.length - 1].players;
        List<PlayerInHand> removed = Arrays.stream(oldPlayers).filter(p -> p.player.balance < game.current.required).collect(Collectors.toList());
        if (removed.size() != 0) {
            // Create sidepot(s)
            List<Float> potBets = removed.stream().map(p -> p.player.balance).distinct().sorted().skip(1).collect(Collectors.toList());
            potBets.add(game.current.required);
            int startPot = game.current.sidepots.length;
            game.current.sidepots = Arrays.copyOf(game.current.sidepots, startPot + potBets.size());
            float lastBet = game.current.maxBet = startPot == 0 ? Arrays.stream(oldPlayers).map(p -> p.player.balance).min(Float::compare).get() : game.current.sidepots[startPot - 1].bet;
            for (int i = 0; i < potBets.size(); i++) {
                List<PlayerInHand> players = Arrays.stream(oldPlayers).filter(new Game.DynamicPlayerMinBalance(potBets.get(i))).collect(Collectors.toList());
                PlayerInHand[] newPlayers = new PlayerInHand[players.size()];
                for (int j = 0; j < players.size(); j++)
                    newPlayers[j] = players.get(j);
                game.current.sidepots[startPot + i] = new Sidepot(newPlayers, potBets.get(i) - lastBet);
                lastBet = potBets.get(i);
            }
        }
        game.current.lastToBet = player.player;
        game.current.lastBet = bet;
        callOrCheck();
    }

    // Removes an element from the given array
    private <T> T[] remove(T[] src, T p) {
        List<T> list = Arrays.stream(src).filter(_p -> _p != p).collect(Collectors.toList());
        T[] out = Arrays.copyOfRange(src, 0, list.size());
        for (int i = 0; i < list.size(); i++)
            out[i] = list.get(i);
        return out;
    }
}
