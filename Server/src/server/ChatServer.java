package server;

import backend.ChatHost;
import backend.ChatPacket;
import backend.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * server della ChatRoom
 */
public class ChatServer extends ChatHost {

    private int group_port;
    private int user_count = 0;
    private HashMap<Integer, User> user_list;

    /**
     * Istanzia il server
     * Associa il server al gruppo multicast specificato e alle rispettive porte
     *
     * @param group_address L'indirizzo del gruppo
     * @param group_port    La porta del gruppo multicast
     * @param local_port    La porta in ascolto
     * @throws IOException In caso di errore durante l'inizializzazione del multicastSocket
     */
    public ChatServer(InetAddress group_address, int group_port, int local_port) throws IOException {
        super(local_port, group_address);
        this.group_port = group_port;
        user_list = new HashMap<Integer, User>();
        startReceiver();
    }

    /**
     * Istanzia il server
     * Associa il server al gruppo multicast specificato e imposta una qualunque porta in ascolto tra le disponibili
     *
     * @param group_address
     * @param group_port
     * @throws IOException In caso di errore durante l'inizializzazione del multicastSocket
     */
    public ChatServer(InetAddress group_address, int group_port) throws IOException {
        super(group_address);
        this.group_port = group_port;
        user_list = new HashMap<Integer, User>();
        startReceiver();
    }

    /**
     * Riceve e restituisce un datagramPacket
     * Questo è un metodo bloccante: resta in attesa di un messaggio fin quando non lo riceve
     *
     * @return il datagramPacket ricevuto
     */
    @Override
    protected DatagramPacket receiveMessage() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket received_packet = new DatagramPacket(buf, buf.length);
        receive(received_packet);
        return received_packet;
    }

    /**
     * Stampa un messaggio in standard output, specificando data e ora corrente
     *
     * @param message Il messaggio da stampare
     */
    public void log(String message) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        System.out.println(String.format("[%s] %s", timeStamp, message));
    }

    /**
     * Protocollo di comunicazione del server
     * Elabora un pacchetto datagram in entrata, generando una risposta e effettuando le dovute operazioni
     *
     * @param received_packet Il pacchetto in ingresso
     */
    @Override
    protected void processMessage(DatagramPacket received_packet) {
        ChatPacket received_message = null;
        try {
            received_message = (ChatPacket) ChatPacket.deserialize(received_packet.getData());
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            return;
        }

        try {
            switch (received_message.message_type) {
                case MESSAGE: {
                    if (user_list.containsKey(received_message.id)) {
                        sendMessage(received_message.message_content, received_message.id, group_address, group_port);
                        log(String.format("(%s:%d) %s#%d: %s",
                                received_packet.getAddress().getHostAddress(),
                                received_packet.getPort(),
                                user_list.get(received_message.id).username,
                                received_message.id,
                                received_message.message_content));
                        break;
                    }
                }
                case REGISTRATION_REQUEST: {
                    log(String.format("Richiesta di registrazione da parte di %s:%d con nome %s",
                            received_packet.getAddress().getHostAddress(),
                            received_packet.getPort(), received_message.message_content));
                    if (nameAlreadyExists(received_message.message_content)) {
                        denyRegistration(
                                received_message.message_content,
                                received_packet.getAddress(),
                                received_packet.getPort());
                        log("Username occupato. Registrazione rifiutata.");
                    } else {
                        acceptRegistration(
                                received_message.message_content,
                                received_packet.getAddress(),
                                received_packet.getPort());
                        sendServerMessage(received_message.message_content + "si è connesso alla chat");
                    }
                    break;
                }
                case DISCONNECT_MESSAGE: {
                    if (user_list.containsKey(received_message.id)) {
                        log(String.format("(%s:%d) %s#%d si è disconnesso.",
                                received_packet.getAddress().getHostAddress(),
                                received_packet.getPort(), user_list.get(received_message.id).username, received_message.id));
                        sendServerMessage(user_list.get(received_message.id).username + " si è disconnesso");
                        user_list.remove(received_message.id);
                    }
                }
                case SERVER_MESSAGE:
                    break;
                case REGISTRATION_ACCEPTED:
                    break;
                case REGISTRATION_DENIED:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Controlla se un username è già stato registrato nella chatroom, controllando l'HashMap user_list
     *
     * @param username L'username da controllare
     * @return true se l'username esiste già, altrmenti false
     */
    private boolean nameAlreadyExists(String username) {
        for (User user : user_list.values()) {
            if (user.username.equalsIgnoreCase(username))
                return true;
        }
        return false;
    }

    /**
     * Invia un messaggio di servizio alla chatroom
     *
     * @param message_content Contenuto del messaggio
     * @throws IOException In caso di errore durante l'invio del messaggio
     */
    void sendServerMessage(String message_content) throws IOException {
        ChatPacket message = new ChatPacket(ChatPacket.Type.SERVER_MESSAGE, message_content);
        byte[] buffer = ChatPacket.serialize(message);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group_address, group_port);
        send(packet);
    }

    /**
     * Invia un messaggio alla chatroom, specificandone mittente e contenuto del messaggio
     *
     * @param message_content  Contenuto del messaggio
     * @param id               Id del mittente
     * @param receiver_address Indirizzo mittente (o gruppo)
     * @param receiver_port    Porta mittente (o gruppo)
     * @throws IOException In caso di errore durante l'invio del messaggio
     */
    void sendMessage(String message_content, int id, InetAddress receiver_address, int receiver_port) throws IOException {
        ChatPacket message = new ChatPacket(ChatPacket.Type.MESSAGE, id, user_list.get(id).username, message_content);
        byte[] buffer = ChatPacket.serialize(message);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiver_address, receiver_port);
        send(packet);
    }

    /**
     * Accetta la registrazione di un client e gli assegna un id
     *
     * @param username       Username di registrazione
     * @param client_address Indirizzo del client
     * @param client_port    Porta del client
     * @throws IOException In caso di errore durante l'invio del messaggio di avvenuta registrazione
     */
    private void acceptRegistration(String username, InetAddress client_address, int client_port) throws IOException {
        user_count++;
        ChatPacket message = new ChatPacket(ChatPacket.Type.REGISTRATION_ACCEPTED, user_count, username);
        byte[] buffer = ChatPacket.serialize(message);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_address, client_port);
        user_list.put(user_count, new User(username, client_address, client_port));
        log(String.format("Registrazione effettuata per %s. È stato assegnato l'id %d", username, user_count));
        send(packet);
    }

    /**
     * Nega la registrazione di un client
     *
     * @param username       Username di registrazione
     * @param client_address Indirizzo del client
     * @param client_port    Porta del client
     * @throws IOException In caso di errore durante l'invio del messaggio
     */
    public void denyRegistration(String username, InetAddress client_address, int client_port) throws IOException {
        ChatPacket message = new ChatPacket(ChatPacket.Type.REGISTRATION_DENIED, username);
        byte[] buffer = ChatPacket.serialize(message);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_address, client_port);
        send(packet);
    }

    /**
     * @return Indirizzo del gruppo
     */
    public InetAddress getGroupAddress() {
        return group_address;
    }

    /**
     * @return Porta del gruppo
     */
    public int getGroup_port() {
        return group_port;
    }
}
