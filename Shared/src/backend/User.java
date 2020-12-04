package backend;

import java.net.InetAddress;

/**
 * Utente della chatroom
 */
public class User {
    public String username;
    public InetAddress address;
    public int port;

    /**
     * @param username L'username dell'utente
     * @param address  L'indirizzo dell'utente
     * @param port     La porta dell'utente
     */
    public User(String username, InetAddress address, int port) {
        this.username = username;
        this.address = address;
        this.port = port;
    }

}
