package violofsson.orange.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class ClientFXController {
    @FXML
    HBox categoryPanel;

    @FXML
    ComboBox<String> categoryChooser;

    @FXML
    Button categoryButton;

    @FXML
    Text playerOne;

    @FXML
    Text playerTwo;

    @FXML
    TextArea messagePanel;

    @FXML
    GridPane buttonPanel;

    private ClientSession session;

    @FXML
    void chooseAnswer(ActionEvent actionEvent) {
        String answer = ((Button) actionEvent.getSource()).getText();
        session.write(answer);
    }

    @FXML
    void chooseCategory() {
        String chosen = categoryChooser.getSelectionModel().getSelectedItem();
        session.write(chosen);
    }
}
