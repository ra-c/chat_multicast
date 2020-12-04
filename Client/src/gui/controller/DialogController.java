package gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller per la finestra di dialogo per la connessione
 */
public class DialogController {

    @FXML
    private Button connectButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField groupAddressField;

    @FXML
    private TextField groupPortField;

    @FXML
    private TextField serverAddressField;

    @FXML
    private TextField serverPortField;

    @FXML
    private TextField usernameField;

    private Stage stage;

    public String[] parameters;

    public void initialize() {
    }

    /**
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Chiude la finestra
     *
     * @param event
     */
    @FXML
    void closeDialog(ActionEvent event) {
        stage.close();
    }

    /**
     * Estrae i dati immessi nei campi di testo affinchè possano essere elaborati da altri controller, poi chiude la finestra.
     * Se i dati immessi non sono validi la finestra non si chiude e riporta messaggio d'errore.
     *
     * @param event
     */
    @FXML
    void submit(ActionEvent event) {
        String serverAddress = serverAddressField.getText().trim();
        String serverPort = serverPortField.getText().trim();
        String groupAddress = groupAddressField.getText().trim();
        String groupPort = groupPortField.getText().trim();
        String username = usernameField.getText().trim();

        if (serverPort.isEmpty() || serverAddress.isEmpty() || groupAddress.isEmpty() ||
                groupPort.isEmpty() || username.isEmpty()) {
            errorLabel.setText("Compilare tutti i campi.");
        } else if (username.length() < 3 || username.length() > 32) {
            errorLabel.setText("Username non valido (3-32 caratteri)");
        } else {
            parameters = new String[]{serverAddress, serverPort, groupAddress, groupPort, username};
            closeDialog(event);
        }
    }


    /**
     * Inizializza lo stage una volta che tutti gli elementi necessari sono caricati
     */
    public void initializeStage() {
        stage = (Stage) connectButton.getScene().getWindow();
        stage.setOnShowing(WindowEvent -> {
            parameters = null;
        });
    }
}
