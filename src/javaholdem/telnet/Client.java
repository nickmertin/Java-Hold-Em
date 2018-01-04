/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * telnet/Client.java
 * Sets up communication with a new client, and represents the client within a game
 */

package javaholdem.telnet;

import javaholdem.textbase.Utilities;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

final class Client extends Thread {
    private boolean disposed;
    private Socket client;
    Scanner s;
    PrintStream w;

    // Creates a new client from the given socket
    public Client(Socket client) throws IOException {
        this.client = client;
        s = new Scanner(client.getInputStream());
        w = new PrintStream(client.getOutputStream());
        System.out.printf("Received connection from %s\n", client.getRemoteSocketAddress().toString());
        disposed = false;
    }

    // Prints a message to the client
    public synchronized void print(String msg) {
        w.print(msg);
    }

    // Sets up communication with the client
    @Override
    public void run() {
        w.print("Welcome to Java Hold 'Em!\r\n" +
                "Copyright (c) 2016 Nicholas Mertin\r\n" +
                "\r\n" +
                "Would you like to see instructions before you play? [y/N] ");
        if (s.nextLine().toLowerCase().contains("y"))
            Utilities.printInstructions(w);
        if (Utilities.get(s, w, "[new/join] ", c -> c.equals("new") || c.equals("join"), String::toLowerCase).equals("new"))
            new Instance(this).start();
        else
            Utilities.get(s, w, "Please enter the unique name of the instance: ", this::join, String::new);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!disposed)
            dispose();
    }

    // Closes communication with the client
    private void dispose() {
        s.close();
        w.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Attempts to join the specified game
    private boolean join(String name) {
        synchronized (Main.instanceMap) {
            return Main.instanceMap.containsKey(name) && Main.instanceMap.get(name).join(this);
        }
    }
}
