package client;

import backend.*;
import interfaces.ClientObserver;
import javafx.application.Platform;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.*;

/**
 * Classe che definisce i client della chatroom.
 */
public class ChatClient extends ChatHost {
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 32;

    private InetAddress server_address;
    private int server_port;

    private int client_id = -1;
    private String username;

    private ClientObserver observer;

    /**
     * Istanzia un oggetto della classe ChatClient, imposta i dovuti parametri e tenta la registrazione con il server.
     *
     * @param group_address  Indirizzo del gruppo multicast
     * @param listening_port Porta in ascolto dal client (La porta a cui il gruppo multicast invierà pacchetti)
     * @param server_address Indirizzo del server
     * @param server_port    Porta del server
     * @param username       Username di registrazione
     * @param observer       Riferimento a un'istanza di una classe che implementa l'interfaccia ClientObserver, a cui notificare eventi
     * @throws IOException      In caso di errore durante la connessione o la registrazione
     * @throws TimeoutException In caso di mancata risposta dal server
     */
    public ChatClient(InetAddress group_address, int listening_port,
                      InetAddress server_address, int server_port, String username, ClientObserver observer)
            throws IOException, TimeoutException {
        super(listening_port);
        setGroup_address(group_address);
        setServer_address(server_address);
        setServer_port(server_port);
        setInterface(InetAddress.getLocalHost());
        try {
            Register(username);
            this.observer = observer;
            startReceiver();
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * Metodo privato chiamato all'interno del costruttore.
     * Effettua la registrazione del client al server specificando l'username scelto
     * In caso di errore durante la registrazione o di registrazione rifiutata, viene lanciata una IOException
     *
     * @param username Username di registrazione
     * @throws IOException      In caso di errore durante la registrazione, o di registrazione rifiutata
     * @throws TimeoutException In caso di mancata risposta dal server
     */
    private void Register(String username) throws IOException, TimeoutException {
        setUsername(username);

        ChatPacket registration_message = new ChatPacket(ChatPacket.Type.REGISTRATION_REQUEST, username);
        byte[] buffer = new byte[0];
        try {
            buffer = ChatPacket.serialize(registration_message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, getServer_address(), getServer_port());
        try {
            send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<ChatPacket> registrationTask = () -> {
            while (true) {
                DatagramPacket received = receiveMessage();

                ChatPacket received_message = null;
                try {
                    received_message = (ChatPacket) ChatPacket.deserialize(received.getData());
                } catch (IOException | ClassNotFoundException | NullPointerException e) {
                    e.printStackTrace();
                    continue;
                }
                if (received_message.message_content.equals(username)) {
                    if (received_message.message_type == ChatPacket.Type.REGISTRATION_ACCEPTED || received_message.message_type == ChatPacket.Type.REGISTRATION_DENIED)
                        return received_message;
                }
            }
        };

        Future<ChatPacket> future = executorService.submit(registrationTask);
        try {
            ChatPacket received_message = future.get(10, TimeUnit.SECONDS);
            if (received_message.message_type == ChatPacket.Type.REGISTRATION_ACCEPTED) {
                client_id = received_message.id;
                joinGroup(group_address);
                return;
            }
            if (received_message.message_type == ChatPacket.Type.REGISTRATION_DENIED) {
                throw new ConnectException("Registrazione rifiutata: username già esistente");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    /**
     * Invia un messaggio al server, che inoltrerà al gruppo multicast
     *
     * @param message_content Contenuto del messaggio
     */
    public void sendMessage(String message_content) {
        ChatPacket message = new ChatPacket(ChatPacket.Type.MESSAGE, client_id, message_content);
        byte[] buffer;
        try {
            buffer = ChatPacket.serialize(message);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, server_address, server_port);
            send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Riceve un datagram e lo restituisce
     *
     * @return Il datagramPacket ricevuto
     */
    @Override
    protected DatagramPacket receiveMessage() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket received_packet = new DatagramPacket(buf, buf.length);
        receive(received_packet);
        return received_packet;

    }

    /**
     * Il protocollo di comunicazione del client.
     * Dato un pacchetto datagram inserito come parametro, il client elabora la risposta e effettua le dovute operazioni
     *
     * @param received_packet Il pacchetto in ingresso
     */
    @Override
    protected void processMessage(DatagramPacket received_packet) {
        ChatPacket received_message = null;


        if (received_packet == null) {
            return;
        }

        try {
            received_message = (ChatPacket) ChatPacket.deserialize(received_packet.getData());
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            return;
        }

        if (received_message == null) {
            return;
        }

        switch (received_message.message_type) {
            case MESSAGE: {
                if (isRegistered()) {
                    ChatPacket finalReceived_message = received_message;
                    Platform.runLater(() -> observer.messageReceived(finalReceived_message));
                }
                break;
            }
            case REGISTRATION_REQUEST:
                break;
            case REGISTRATION_ACCEPTED:
                break;
            case REGISTRATION_DENIED:
                break;
            case SERVER_MESSAGE: {
                if (isRegistered()) {
                    ChatPacket finalReceived_message = received_message;
                    Platform.runLater(() -> observer.serverMessageReceived(finalReceived_message));
                }
                break;
            }

        }
    }

    /**
     * Controlla se il client è già registrato
     *
     * @return true se il suo client_id è maggiore di 0, altrimenti false
     */
    public boolean isRegistered() {
        return client_id > 0;
    }

    /**
     * Invia un messaggio di disconnessione al server, interrompe il backend.backend.Receiver thread e chiude il socket
     */
    public void disconnect() {
        try {
            byte[] buffer = ChatPacket.serialize(new ChatPacket(ChatPacket.Type.DISCONNECT_MESSAGE, getClient_id()));
            send(new DatagramPacket(buffer, buffer.length, getServer_address(), getServer_port()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        close();
    }

    /**
     * @return L'indirizzo del server
     */
    public InetAddress getServer_address() {
        return server_address;
    }

    /**
     * @param server_address L'indirizzo del server
     */
    public void setServer_address(InetAddress server_address) {
        this.server_address = server_address;
    }

    /**
     * @return La porta del server
     */
    public int getServer_port() {
        return server_port;
    }

    /**
     * @param server_port La porta del server
     */
    public void setServer_port(int server_port) {
        this.server_port = server_port;
    }

    /**
     * @return L'indirizzo del gruppo
     */
    public InetAddress getGroup_address() {
        return group_address;
    }

    /**
     * @param group_address L'indirizzo del gruppo
     */
    public void setGroup_address(InetAddress group_address) {
        this.group_address = group_address;
    }

    /**
     * @return L'id del client
     */
    public int getClient_id() {
        return client_id;
    }

    /**
     * Imposta l'id del client.
     * Se il client è già registrato, non viene effetuata alcuna operazione
     *
     * @param client_id L'id del client
     */
    public void setClient_id(int client_id) {
        if (isRegistered()) {
            return;
        }
        this.client_id = client_id;
    }

    /**
     * @return L'username di registrazione
     */
    public String getUsername() {
        return username;
    }

    /**
     * Imposta l'username di registrazione
     *
     * @param username Username di registrazione
     * @throws IllegalArgumentException Se l'username non rispetta i vincoli di lunghezza
     */
    public void setUsername(String username) throws IllegalArgumentException {
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH)
            throw new IllegalArgumentException("Lunghezza username non valida");

        this.username = username;
    }
}

