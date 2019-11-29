package violofsson.orange.client;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientFX extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Orange Quiz");
        initRootLayout();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientFXRoot.fxml"));
            Parent root = loader.load();
            ClientFXController controller = loader.getController();
            ClientSession session = controller.getSession();
            // TODO Ta reda på hur det här ens fungerar
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    session.run();
                    return null;
                }
            };
            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
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
