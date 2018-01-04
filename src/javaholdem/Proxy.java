/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Proxy.java
 * Provides a common interface for communicating with all players in any game interface
 */

package javaholdem;

public interface Proxy {
    // Writes the given message to all players
    void log(String msg);

    // Waits for all players' approval before continuing (blocking call)
    void pause();

    // Clears the terminal for all players
    void clear();
}
