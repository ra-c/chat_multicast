package interfaces;

import backend.ChatPacket;

/**
 * Interfaccia per definire osservatori del client.
 */
public interface ClientObserver {
    /**
     * Chiamata quando il client riceve un messaggio
     *
     * @param message Messaggio ricevuto
     */
    void messageReceived(ChatPacket message);

    /**
     * Chiamata quando il client riceve un messaggio da parte del server
     *
     * @param message Messaggio ricevuto
     */
    void serverMessageReceived(ChatPacket message);
}
