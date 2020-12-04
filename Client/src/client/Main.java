package client;

import gui.controller.MainController;
import gui.model.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto d'inizio per l'esecuzione del client
 */
public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/view/mainScene.fxml"));
        Model model = new Model();
        MainController controller = new MainController(model);
        loader.setController(controller);
        Parent root = loader.load();
        primaryStage.setTitle("Chat Room Multicast");
        primaryStage.setOnCloseRequest(event -> controller.shutdown());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}