package violofsson.orange.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientFX extends Application {
    private Stage primaryStage;
    private BorderPane root;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Orange Quiz");
        initRootLayout();
    }

    public void initRootLayout() {
        try {
            root = FXMLLoader.load(getClass().getResource("ClientFXRoot.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
