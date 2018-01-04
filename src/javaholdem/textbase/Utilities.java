/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * textbase/Utilities.java
 * Provides the common view controller code for text-based interfaces
 */

package javaholdem.textbase;

import javaholdem.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Utilities {
    // Gets input from the given scanner, prompting, parsing, and checking against a validation predicate
    public static <T> T get(Scanner s, PrintStream out, String prompt, Predicate<T> predicate, Function<String, T> parser) {
        while (true) {
            try {
                // Read line, try to parse, and check if it passes the predicate
                out.print(prompt);
                T r = parser.apply(s.nextLine());
                if (predicate.test(r))
                    return r;
                out.println("Invalid input!");
            } catch (Exception e) {
                out.println("Invalid input!");
            }
        }
    }

    // Prints the game instructions to the given stream
    public static void printInstructions(PrintStream out) {
        InputStream rules = Utilities.class.getResourceAsStream("rules.txt");
        Scanner rs = new Scanner(rules);
        while (rs.hasNextLine())
            out.println(rs.nextLine());
        rs.close();
        try {
            rules.close();
        } catch (IOException e) {
            out.println("Error: could not read rules file!");
        }
    }

    // Sets up a game through the given scanner and output stream, using the given proxy and players set
    public static Game setupGame(Scanner s, PrintStream out, Proxy proxy, Player[] players) {
        float start = get(s, out, "How much money will each player start with? $", f -> f > 0, Float::parseFloat);
        if (players == null) {
            ArrayList<Player> playersList = new ArrayList<>(9);
            String[] nums = new String[]{"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"};
            do {
                playersList.add(new Player(get(s, out, String.format("What is the name of the %s player? ", nums[playersList.size()]), n -> n.length() > 0, String::trim), start));
                if (playersList.size() > 1)
                    out.print("Add another player? [Y/n] ");
            } while (playersList.size() <= 1 || !s.nextLine().toLowerCase().contains("n"));
            players = new Player[playersList.size()];
            for (int i = 0; i < playersList.size(); ++i)
                players[i] = playersList.get(i);
        }
        else
            for (Player player : players)
                player.balance = start;
        final Player[] pArray = players;
        float ante = get(s, out, "What will be the ante? $", f -> f >= 0, Float::parseFloat);
        int blindCount = get(s, out, "How many blinds will there be? ", c -> c >= 0 && c <= pArray.length, Integer::parseInt);
        float[] blinds;
        if (blindCount != 0) {
            float blindBase = get(s, out, "What is the smallest blind? ", f -> f > 0 && f * blindCount + ante <= start, Float::parseFloat);
            blinds = new float[blindCount];
            for (int i = 0; i < blinds.length; i++)
                blinds[i] = blindBase * (blindCount - i);
        }
        else
            blinds = new float[0];
        return new Game(players, ante, blinds, proxy);
    }

    // Runs the main turn interface through the given scanner and stream for the given context
    public static void runPlayer(Scanner s, PrintStream out, PlayerContext ctx) {
        Game game = ctx.game;
        // Show info
        out.println("Players in the current hand:");
        for (PlayerInHand p : game.current.players)
            out.printf("\t%s\r\n", p.player.toString());
        out.printf("Main pot: $%.2f\r\n", game.current.mainPotBalance);
        out.println("Side pots:");
        for (Sidepot sidepot : game.current.sidepots)
            out.println(sidepot);
        out.println("Community cards:");
        for (Card card : game.current.communityCards)
            out.printf("\t%s\r\n", card.toString());
        out.println("Pocket:");
        out.printf("\t%s\r\n\t%s\r\n", ctx.player.card1.toString(), ctx.player.card2.toString());
        out.printf("Current bet: $%.2f\r\nYour bet: $%.2f\r\nMinimum bet: $%.2f\r\nYour balance: $%.2f\r\n", game.current.required, ctx.player.bet, game.current.lastBet, ctx.player.player.balance);
        // Wait for command
        String cStay = game.current.required - ctx.player.bet == 0 || ctx.player.player.balance == 0 ? "check" : "call", cUp = game.current.required == 0 ? "bet" : "raise";
        String[] commands = game.current.lastBet > ctx.maxBet || ctx.player.player.balance == 0 || game.current.lastBet + game.current.required > ctx.player.player.balance ? new String[]{"fold", cStay} : new String[]{"fold", cStay, cUp};
        switch (get(s, out, String.format("What would you like to do? [%s] ", Arrays.stream(commands).reduce((x, y) -> x + "/" + y).get()), Arrays.asList(commands)::contains, String::toLowerCase)) {
            case "fold":
                ctx.fold();
                break;
            case "check":
            case "call":
                ctx.callOrCheck();
                break;
            case "bet":
            case "raise":
                ctx.betOrRaise(get(s, out, "How much? $", new Game.DynamicFloatRange(0, Math.min(ctx.maxBet, ctx.player.player.balance) + game.ante / 2, game.ante), Float::parseFloat));
                break;
        }
    }
}
