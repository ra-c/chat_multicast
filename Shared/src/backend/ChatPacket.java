package backend;

import java.io.*;

/**
 * Classe che definisce i messaggi inviati nella chatroom
 */
public class ChatPacket implements Serializable {

    /**
     * Il tipo di messaggio
     */
    public enum Type {MESSAGE, REGISTRATION_REQUEST, REGISTRATION_ACCEPTED, REGISTRATION_DENIED, SERVER_MESSAGE, DISCONNECT_MESSAGE}

    public Type message_type;
    public int id;
    public String username;
    public String message_content;

    /**
     * @param type    Il tipo del messaggio
     * @param message Il contenuto del messaggio
     */
    public ChatPacket(Type type, String message) {
        message_type = type;
        message_content = message;
    }

    /**
     * @param type Il tipo del messaggio
     * @param id   L'id dell'utente associato al messaggio
     */
    public ChatPacket(Type type, int id) {
        message_type = type;
        this.id = id;
    }

    /**
     * @param type     Il tipo del messaggio
     * @param id       L'id dell'utente associato al messaggio
     * @param username L'username del'utente associato al messaggio
     * @param content  Il contenuto del messaggio
     */
    public ChatPacket(Type type, int id, String username, String content) {
        message_type = type;
        this.id = id;
        message_content = content;
        this.username = username;
    }

    /**
     * @param type    Il tipo del messaggio
     * @param id      L'id dell'utente associato al messaggio
     * @param content Il contenuto del messaggio
     */
    public ChatPacket(Type type, int id, String content) {
        message_type = type;
        this.id = id;
        message_content = content;
    }


    /**
     * Serializza l'oggetto, trasformandolo in una sequenza di byte trasferibile in rete
     *
     * @param obj L'oggetto da serializzare
     * @return Un array di byte che rappresenta l'oggetto serializzato
     * @throws IOException In caso di errore durante la serializzazione
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * Deserializza una sequenza di byte, trasformandolo in un oggetto caricabile in memoria.
     *
     * @param data Sequenza di byte in ingresso
     * @return L'oggetto deserializzato
     * @throws IOException            In caso di errore durante la deserializzazione
     * @throws ClassNotFoundException In caso la sequenza di byte non rappresenti nessuna classe
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }


}
