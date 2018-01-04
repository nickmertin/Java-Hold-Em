/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * telnet/Instance.java
 * Represents and manages a game instance
 */

package javaholdem.telnet;

import javaholdem.*;
import javaholdem.textbase.Utilities;

import java.util.HashMap;
import java.util.function.Predicate;

final class Instance extends Thread {
    private volatile boolean joinable;
    HashMap<Player, Client> clients;
    TelnetProxy proxy;
    Game game;

    // Creates a new game instance, communicating with the given client to set it up
    public Instance(Client creator) {
        clients = new HashMap<>();
        clients.put(new Player(Utilities.get(creator.s, creator.w, "What is your name? ", ((Predicate<String>) String::isEmpty).negate(), String::new), 0), creator);
        proxy = new TelnetProxy(this);
        Main.instanceMap.put(Utilities.get(creator.s, creator.w, "Please create a unique name for the instance: ", s -> {
            synchronized (Main.instanceMap) {
                if (s.length() > 0 && !Main.instanceMap.containsKey(s)) {
                    Main.instanceMap.put(s, this);
                    return true;
                }
            }
            return false;
        }, String::new), this);
        proxy.clear();
        joinable = true;
        creator.w.println("Please wait for all players to join, then press <enter> to continue.");
        creator.s.nextLine();
        joinable = false;
        proxy.log("Setting up game");
        game = Utilities.setupGame(creator.s, creator.w, proxy, clients.keySet().toArray(new Player[clients.size()]));
    }

    // Attepts to add the given client to the game
    public synchronized boolean join(Client client) {
        if (joinable) {
            client.w.println("Players currently in the game:");
            clients.keySet().stream().map(p -> p.name).forEach(client.w::println);
            String name = Utilities.get(client.s, client.w, "What is your name? ", ((Predicate<String>)clients::containsKey).negate(), String::new);
            clients.put(new Player(name, 0), client);
            proxy.log(String.format("%s has joined the game!", name));
        }
        return joinable;
    }

    // Manages the running game
    @Override
    public void run() {
        PlayerContext ctx;
        while ((ctx = game.next()) != null) {
            proxy.log(String.format("It is now %s's turn!", ctx.player.player.name));
            Client c = clients.get(ctx.player.player);
            c.print("\n");
            Utilities.runPlayer(c.s, c.w, ctx);
        }
    }
}
