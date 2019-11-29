package violofsson.orange.client;

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
import java.util.ArrayList;
import java.util.List;

public class ClientFXController extends Thread {
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
        setAnswersDisable(true);
        session.write(answer);
    }

    @FXML
    void chooseCategory() {
        String chosen = categoryChooser.getSelectionModel().getSelectedItem();
        setCategoryDisable(true);
        session.write(chosen);
    }

    @FXML
    public void initialize() throws IOException {
        // TODO IllegalStateException
        session = new ClientSession();
        this.start();
    }

    @Override
    public void run() {
        Object obj;

        try {
            while ((obj = session.read()) != null) {
                if (obj instanceof Question) {
                    Question question = (Question) obj;
                    displayQuestion(question);
                } else if (obj instanceof ServerMessage) {
                    ServerMessage fromServer = (ServerMessage) obj;
                    processServerMessage(fromServer);
                } else if (obj instanceof String) {
                    String message = (String) obj;
                    displayMessage(message);
                } else if (obj instanceof Integer[]) {
                    Integer[] points = (Integer[]) obj;
                    displayPoints(points);
                } else if (obj instanceof ArrayList) {
                    // Kontrollera typer!
                    ArrayList<List> lista = (ArrayList) obj;
                    List<Integer> playerOneHistory = lista.get(0);
                    List<Integer> playerTwoHistory = lista.get(1);
                    /*String playerOneText = getScoreSummary("Spelare 1",
                            playerOneHistory);
                    String playerTwoText = getScoreSummary("Spelare 2",
                            playerTwoHistory);
                    JOptionPane.showMessageDialog(this, playerOneText + "\n\n" + playerTwoText);*/
                    // TODO Ordna en riktig översikt
                    System.out.println(playerOneHistory.toString());
                    System.out.println(playerTwoHistory);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void displayMessage(String msg) {
        messagePanel.setText(msg);
    }

    private void displayPoints(Integer[] points) {
        playerOne.setText("P1 : " + points[0]);
        playerTwo.setText("P2 : " + points[1]);
    }

    void displayQuestion(Question q) {
        displayMessage(q.getQuestion());
        List<String> alternatives = q.getAlternatives();
        for (int i = 0; i < buttonPanel.getChildren().size(); i++) {
            Button b = (Button) buttonPanel.getChildren().get(i);
            b.setText(alternatives.get(i));
        }
        setAnswersDisable(false);
    }

    private Alert getMessageDialog(String title, String header, String content) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog;
    }

    private void processServerMessage(ServerMessage fromServer) {
        if (fromServer.HEADER == ServerMessage.Headers.WELCOME) {
            if (fromServer.MESSAGE.contains("1")) {
                playerOne.setText(fromServer.MESSAGE);
                playerTwo.setText("Player 2");
            } else {
                playerTwo.setText(fromServer.MESSAGE);
                playerOne.setText("Player 1");
            }
        } else if (fromServer.HEADER == ServerMessage.Headers.WAIT) {
            setAnswersDisable(true);
            setCategoryDisable(true);
            displayMessage(fromServer.MESSAGE);
        } else if (fromServer.HEADER == ServerMessage.Headers.CHOOSE_CATEGORY) {
            String[] categories = fromServer.MESSAGE.split(";");
            categoryChooser.getItems().clear();
            for (String s : categories) {
                categoryChooser.getItems().add(s);
            }
            setCategoryDisable(false);
            displayMessage("Choose category");
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_WIN) {
            getMessageDialog(null, "You win!", "Congratulations!").showAndWait();
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_LOSE) {
            getMessageDialog(null, "You lose!", "Too bad!").showAndWait();
        } else if (fromServer.HEADER == ServerMessage.Headers.YOU_TIED) {
            getMessageDialog(null, "You tied!", "How unexpected!").showAndWait();
        } else {
            displayMessage(fromServer.MESSAGE);
        }
    }

    void setAnswersDisable(boolean b) {
        buttonPanel.setDisable(b);
    }

    void setCategoryDisable(boolean b) {
        categoryPanel.setDisable(b);
    }
}
