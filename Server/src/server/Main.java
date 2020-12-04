package server;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Punto d'inizio per l'esecuzione del server
 */
public class Main {


    public static void main(String[] args) {
        ChatServer server;
        try {
            if (args.length == 1) {
                server = new ChatServer(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
            } else if (args.length == 3) {
                server = new ChatServer(InetAddress.getByName(args[0]), Integer.parseInt(args[1], Integer.parseInt(args[2])));
            } else {
                System.out.println("Sintassi: java server.server.Main [porta locale] [indirizzo multicast] [porta multicast]");
                server = new ChatServer(InetAddress.getByName("224.1.1.1"), 6667, 6666);
            }
            System.out.println("server in ascolto.");
            System.out.println("Indirizzo server: " + InetAddress.getLocalHost().getHostAddress() + ":" + server.getLocalPort());
            System.out.println("Indirizzo gruppo multicast: " + server.getGroupAddress().getHostAddress() + ":" + server.getGroup_port());
        } catch (IOException e) {
            System.err.println("Indirizzo sconosciuto o non valido.");

        }
    }
}
