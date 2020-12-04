package gui.model;

import client.ChatClient;
import javafx.beans.property.*;
import javafx.beans.property.adapter.*;

import java.net.InetAddress;


/**
 * Dati e propriet&agrave; per l'interfaccia grafica
 */
public class Model {
    public ObjectProperty<ChatClient> client_property = new SimpleObjectProperty<>(null);
    public ReadOnlyIntegerProperty client_id = new SimpleIntegerProperty(0);
    public ReadOnlyObjectProperty<InetAddress> group_address = new SimpleObjectProperty<InetAddress>();
    public ReadOnlyIntegerProperty group_port = new SimpleIntegerProperty(0);

    private ChatClient client;

    /**
     * @return riferimento all'oggetto ChatClient corrente
     */
    public ChatClient getClient() {
        return client;
    }

    /**
     * Imposta il client e effettua le relative associazioni con gli elementi dell'interfaccia grafica
     *
     * @param client Il client da associare
     */
    public void setClient(ChatClient client) {
        this.client = client;
        client_property.setValue(client);

        if (client != null) {
            try {
                client_id = new ReadOnlyJavaBeanIntegerPropertyBuilder().bean(getClient()).name("client_id").build();
                group_address = new ReadOnlyJavaBeanObjectPropertyBuilder<InetAddress>().bean(getClient()).name("group_address").build();
                group_port = new ReadOnlyJavaBeanIntegerPropertyBuilder().bean(getClient()).name("localPort").build();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
