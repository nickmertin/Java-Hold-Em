/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * console/ConsoleProxy.java
 * Provides a proxy for I/O with the console
 */

package javaholdem.console;

import javaholdem.Proxy;

final class ConsoleProxy implements Proxy {
    // Writes the specified message to the console
    @Override
    public void log(String msg) {
        System.out.println(msg);
    }

    // Waits for user approval before continuing (blocking call)
    @Override
    public void pause() {
        System.out.print("Press <enter> to continue...");
        Main.s.nextLine();
    }

    // Clears the console
    @Override
    public void clear() {
        System.out.print("\033[1;1H\033[2J");
    }
}
