package backend;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Classe che si occupa della continua ricezione di pacchetti datagram dalla rete.
 * Questa classe è chiamata dalla classe backend.backend.ChatHost, nel run() vengono chiamati metodi
 * astratti implementati dalle sottoclassi (ChatClient o ChatServer)
 */
public class Receiver implements Runnable {
    private ChatHost host = null;

    /**
     * Istanzia la classe receiver e lo associa al relativo backend.backend.ChatHost
     *
     * @param host Host per cui ricevere datagram
     */
    public Receiver(ChatHost host) {
        this.host = host;
    }

    @Override
    public void run() {
        while (!host.isClosed()) {
            DatagramPacket received = null;
            try {
                received = host.receiveMessage();
                host.processMessage(received);
            } catch (IOException e) {
                System.err.println("Socket chiuso, esco dal thread");
                break;
            }
        }
    }
}