package gui.controller;

import backend.*;
import client.ChatClient;
import gui.model.Model;
import interfaces.ClientObserver;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * Controller per la finestra principale
 */
public class MainController implements ClientObserver {

    @FXML
    private MenuItem connectItem;

    @FXML
    private MenuItem disconnectItem;

    @FXML
    private VBox messageBox;

    @FXML
    private TextField messageField;


    @FXML
    private Font x3;

    @FXML
    private Color x4;

    @FXML
    private Label leftStatus;

    @FXML
    private Label rightStatus;

    @FXML
    private ScrollPane scrollPane;

    private Parent dialogPane;
    private Stage dialog;
    private DialogController dialogController;

    private Model model;

    /**
     * Istanzia il controller e lo associa al model specificato
     *
     * @param model Model da cui ricavare dati e proprietà
     */
    public MainController(Model model) {
        this.model = model;
    }


    /**
     * Chiamata automaticamente una volta istanziata.
     * Carica le finestre di dialogo e effettua associa gli elementi della scena al client.
     *
     * @throws IOException
     */
    public void initialize() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/view/dialog.fxml"));
        dialogPane = loader.load();
        dialogController = loader.getController();

        connectItem.disableProperty().bind(model.client_property.isNotNull());
        disconnectItem.disableProperty().bind(model.client_property.isNull());
        messageField.disableProperty().bind(model.client_property.isNull());

        leftStatus.visibleProperty().bind(model.client_property.isNotNull());

        rightStatus.textProperty().bind(Bindings.when(model.client_property.isNull()).then("Non connesso").otherwise("Connesso"));


    }


    /**
     * @return Riferimento allo stage della finestra di dialogo
     */
    private Stage getDialog() {
        if (dialog == null) {
            Scene scene = new Scene(dialogPane);
            dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Connetti...");
            dialog.setScene(scene);
            dialog.initOwner(messageBox.getScene().getWindow());
            dialogController.initializeStage();
        }
        return dialog;
    }

    /**
     * Mostra la finestra di dialogo e attende fin quando questa non viene chiusa, poi restituisce i parametri inseriti
     * all'interno di essa.
     *
     * @return String[] con i parametri inseriti, Null se la finestra è stata chiusa senza inviare dati.
     */
    private String[] showDialog() {
        getDialog().showAndWait();
        return dialogController.parameters;
    }

    /**
     * Apre la finestra di dialogo, attende i parametri di connessione e una volta ricevuti istanzia un oggetto ChatClient
     * e tenta la connessione.
     *
     * @param event
     */
    @FXML
    void openConnectDialog(ActionEvent event) {
        String[] parameters = showDialog(); //Array in ordine: {serverAddress, serverPort, groupAddress, groupPort, username}
        if (parameters != null) {
            model.setClient(null);
            showLocalMessage("Elaboro parametri inseriti...");
            try {
                InetAddress groupAddress = InetAddress.getByName(parameters[2]);
                InetAddress serverAddress = InetAddress.getByName(parameters[0]);
                int serverPort = Integer.parseInt(parameters[1]);
                int groupPort = Integer.parseInt(parameters[3]);

                showLocalMessage("Tentativo di connessione al server...");
                model.setClient(new ChatClient(groupAddress, groupPort, serverAddress, serverPort, parameters[4], this));
                showLocalMessage("Connessione effettuata.");
                leftStatus.textProperty().bind(Bindings.concat(model.group_address.get().getHostAddress(), ":", model.group_port.get()));
            } catch (UnknownHostException e) {
                showLocalMessage("Indirizzo sconosciuto");
            } catch (NumberFormatException e) {
                showLocalMessage("Porta non valida");
            } catch (TimeoutException e) {
                showLocalMessage("Tempo scaduto.");
            } catch (ConnectException e) {
                showLocalMessage(e.getMessage());
            } catch (IOException e) {
                showLocalMessage("Errore durante la connessione");
            }
        }
    }


    /**
     * Effettua la disconnessione col client, se necessario, ed esce dal programma.
     */
    public void shutdown() {
        System.out.println("Chiusura");
        if (model.getClient() != null) {
            disconnect();
        }
        Platform.exit();
    }

    /**
     * Disconnette il client e cancella il suo riferimento
     */
    @FXML
    private void disconnect() {
        model.getClient().disconnect();
        model.setClient(null);
        showLocalMessage("Disconnesso.");
    }

    /**
     * Invia il contenuto della textfield
     *
     * @param event
     */
    @FXML
    void sendMessage(ActionEvent event) {
        if (!messageField.getText().trim().isEmpty()) {
            try {
                model.getClient().sendMessage(messageField.getText());
                messageField.clear();
            } catch (NullPointerException e) {
                showLocalMessage("Client non registrato");
            }
        }
    }

    /**
     * Stampa a video un messaggio dal client
     *
     * @param message Il messaggio da mostrare
     */
    private void showLocalMessage(String message) {
        messageBox.getChildren().add(new LocalMessage(messageBox, message));
        scrollPane.setVvalue(1.0d);
    }

    /**
     * Stampa a video un messaggio da parte di un altro utente nella chatroom
     *
     * @param message Il messaggio da mostrare
     * @param sender  Il nome del mittente
     */
    private void showMessage(String message, String sender) {
        messageBox.getChildren().add(new ReceivedMessage(messageBox, message, sender));
        scrollPane.setVvalue(1.0d);
    }

    /**
     * Metodo chiamato alla ricezione da parte del client di un messaggio
     * Quando chiamato, mostra a video il messaggio ricevuto
     *
     * @param received_message Messaggio ricevuto
     */
    @Override
    public void messageReceived(ChatPacket received_message) {
        showMessage(received_message.message_content, received_message.username);
    }

    /**
     * Metodo chiamato alla ricezione da parte del client di un messaggio da parte del server
     * Quando chiamato, mostra a video il messaggio ricevuto
     *
     * @param received_message Messaggio ricevuto
     */
    @Override
    public void serverMessageReceived(ChatPacket received_message) {
        showLocalMessage(received_message.message_content);
    }


}
