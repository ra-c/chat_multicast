package gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controller per il nodo LocalMessage
 */
public class LocalMessage extends VBox {

    @FXML
    private Label localMessageLabel;

    /**
     * Restituisce il riferimento a un nodo contenente il messaggio inserito e agganciato al nodo specificato.
     *
     * @param node    Il nodo padre a cui agganciarsi
     * @param message Il corpo del messaggio
     */
    public LocalMessage(Node node, String message) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/view/localMessage.fxml"));
        loader.setController(this);
        loader.setRoot(node);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        localMessageLabel.setText(message);
    }

}