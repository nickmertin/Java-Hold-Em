/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * telnet/Main.java
 * Manages the server
 */

package javaholdem.telnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Scanner;

public final class Main {
    static final HashMap<String, Instance> instanceMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.print("Port: ");
        ServerSocket server = new ServerSocket(new Scanner(System.in).nextInt());
        while (true)
            new Client(server.accept()).start();
    }
}
