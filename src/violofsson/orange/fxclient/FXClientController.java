package violofsson.orange.fxclient;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import java.io.IOException;
import java.util.List;

public class FXClientController {
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
    @FXML
    Button continueButton;

    private FXClientConnection connection;
    private String chosenAnswer;

    @FXML
    void chooseAnswer(ActionEvent actionEvent) {
        chosenAnswer = ((Button) actionEvent.getSource()).getText();
        setAnswersDisable(true);
        setContinueDisable(false);
    }

    @FXML
    void chooseCategory() {
        String chosen = categoryChooser.getSelectionModel().getSelectedItem();
        setCategoryDisable(true);
        connection.send(chosen);
    }

    @FXML
    void nextQuestion() {
        setContinueDisable(true);
        connection.send(chosenAnswer);
    }

    @FXML
    public void initialize() throws IOException {
        connection = new FXClientConnection(this);
        // TODO Ta reda på hur det här ens fungerar
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                connection.run();
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    synchronized void displayMessage(String msg) {
        messagePanel.setText(msg);
    }

    synchronized void displayPoints(Integer[] points) {
        playerOne.setText("P1 : " + points[0]);
        playerTwo.setText("P2 : " + points[1]);
    }

    synchronized void displayQuestion(Question q) {
        displayMessage(q.getQuestion());
        List<String> alternatives = q.getAlternatives();
        for (int i = 0; i < buttonPanel.getChildren().size(); i++) {
            Button b = (Button) buttonPanel.getChildren().get(i);
            b.setText(alternatives.get(i));
        }
        setAnswersDisable(false);
    }

    private Alert getMessageDialog(String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(null);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog;
    }

    synchronized void processServerMessage(ServerMessage fromServer) {
        if (fromServer.HEADER == ServerMessage.Headers.WELCOME) {
            playerOne.setText("Player 1");
            playerTwo.setText("Player 2");
            displayMessage(fromServer.BODY);
        } else if (fromServer.HEADER == ServerMessage.Headers.WAIT) {
            setAnswersDisable(true);
            setCategoryDisable(true);
            displayMessage(fromServer.BODY);
        } else if (fromServer.HEADER == ServerMessage.Headers.CHOOSE_CATEGORY) {
            String[] categories = fromServer.BODY.split(";");
            /*categoryChooser.getItems().clear();
            for (String s : categories) {
                categoryChooser.getItems().add(s);
            }
            setCategoryDisable(false);*/
            for (int i = 0; i < categories.length && i < buttonPanel.getChildren().size(); i++) {
                Button btn = (Button) buttonPanel.getChildren().get(i);
                btn.setText(categories[i]);
            }
            displayMessage("Choose category");
            setAnswersDisable(false);
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_WIN) {
            getMessageDialog("You win!", "Congratulations!").showAndWait();
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_LOSE) {
            getMessageDialog("You lose!", "Too bad!").showAndWait();
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_TIED) {
            getMessageDialog("You tied!", "How unexpected!").showAndWait();
        } else {
            displayMessage(fromServer.BODY);
        }
    }

    void setAnswersDisable(boolean b) {
        buttonPanel.setDisable(b);
    }

    void setCategoryDisable(boolean b) {
        categoryPanel.setDisable(b);
    }

    void setContinueDisable(boolean b) {
        continueButton.setDisable(b);
        continueButton.setVisible(!b);
    }
}
