package gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controller per il nodo ReceivedMessage
 */
public class ReceivedMessage extends VBox {

    @FXML
    private Hyperlink usernameLink;

    @FXML
    private Label messageLabel;

    /**
     * Restituisce il riferimento a un nodo (agganciato al nodo padre specificato) contenete il mittente e il contenuto
     * del messaggio
     *
     * @param node     Il nodo padre a cui agganciarsi
     * @param message  Il corpo del messaggio
     * @param username Il nome del mittente del messaggio
     */
    public ReceivedMessage(Node node, String message, String username) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/view/receivedMessage.fxml"));
        loader.setController(this);
        loader.setRoot(node);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        messageLabel.setText(message);
        usernameLink.setText(username);
    }

}
