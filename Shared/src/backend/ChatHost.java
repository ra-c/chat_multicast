package backend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Classe astratta che definisce gli elementi in comune di ChatClient e ChatServer.
 */
public abstract class ChatHost extends MulticastSocket {
    protected InetAddress group_address;

    private Receiver receiver;
    protected Thread receiverThread;

    /**
     * @return
     */
    protected abstract DatagramPacket receiveMessage() throws IOException;

    /**
     * @param received_message
     */
    protected abstract void processMessage(DatagramPacket received_message);

    /**
     * @param port
     * @param group
     * @throws IOException
     */
    public ChatHost(int port, InetAddress group) throws IOException {
        super(port);
        this.group_address = group;
    }

    /**
     * @param listening_port
     * @throws IOException
     */
    public ChatHost(int listening_port) throws IOException {
        super(listening_port);
    }

    /**
     * @param group_address
     * @throws IOException
     */
    public ChatHost(InetAddress group_address) throws IOException {
        this.group_address = group_address;
    }


    /**
     * Crea un'istanza della classe backend.backend.Receiver e lo fa partire in un nuovo thread.
     */
    protected void startReceiver() {
        receiver = new Receiver(this);
        receiverThread = new Thread(receiver);
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    /**
     * Invia un messaggio al relativo indirizzo e alla relativa porta, specificando contenuto del messaggio
     * e id del mittente
     *
     * @param message_content  contenuto del messaggio
     * @param id               Id del mittente
     * @param receiver_address Indirizzo destinatario
     * @param receiver_port    Porta destinatario
     * @throws IOException In caso di errore durante l'invio del messaggio
     */
    void sendMessage(String message_content, int id, InetAddress receiver_address, int receiver_port) throws IOException {
        ChatPacket message = new ChatPacket(ChatPacket.Type.MESSAGE, id, message_content);
        byte[] buffer = ChatPacket.serialize(message);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiver_address, receiver_port);
        send(packet);
    }

}
