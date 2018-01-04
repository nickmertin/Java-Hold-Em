/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * console/Main.java
 * Main view controller for console interface
 */

package javaholdem.console;

import javaholdem.*;
import javaholdem.textbase.Utilities;

import java.util.Scanner;

public final class Main {
    static Scanner s = new Scanner(System.in);
    static ConsoleProxy console = new ConsoleProxy();

    public static void main(String[] args) {
        console.clear();
        System.out.print("Welcome to Java Hold 'Em!\n" +
                "Copyright (c) 2016 Nicholas Mertin\n" +
                "\n" +
                "Would you like to see instructions before you play? [y/N] ");
        if (s.nextLine().toLowerCase().contains("y"))
            Utilities.printInstructions(System.out);
        Game game = Utilities.setupGame(s, System.out, console, null);
        PlayerContext ctx;
        // Main control loop
        while ((ctx = game.next()) != null) {
            // Clear the screen to hide pocket, wait for player
            console.clear();
            System.out.printf("Please pass the computer to %s, then press enter to continue.", ctx.player.player.name);
            s.nextLine();
            Utilities.runPlayer(s, System.out, ctx);
        }
        System.out.printf("%s wins!\n", game.players[0].name);
    }
}
