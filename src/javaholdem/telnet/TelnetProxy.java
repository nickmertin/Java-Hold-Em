/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * telnet/TelnetProxy.java
 * Provides a proxy for I/O with all clients in a game instance
 */

package javaholdem.telnet;

import javaholdem.Proxy;

final class TelnetProxy implements Proxy {
    private Instance instance;

    // Creates a new proxy for the given game instance
    public TelnetProxy(Instance instance) {
        this.instance = instance;
    }

    // Writes the given message, followed by a newline, to each client's terminal
    @Override
    public void log(String msg) {
        instance.clients.values().parallelStream().forEach(c -> c.print(msg + "\r\n"));
    }

    // Waits for each client's approval before continuing (blocking call)
    @Override
    public void pause() {
        instance.clients.values().parallelStream().forEach(c -> {
            c.print("Press <enter> to continue...");
            c.s.nextLine();
        });
    }

    // Clears each client's terminal
    @Override
    public void clear() {
        instance.clients.values().parallelStream().forEach(c -> c.print("\f"));
    }
}
