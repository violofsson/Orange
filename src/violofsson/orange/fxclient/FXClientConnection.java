package violofsson.orange.fxclient;

import javafx.application.Platform;
import violofsson.orange.protocol.ClientConnection;
import violofsson.orange.protocol.GenericClientController;
import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FXClientConnection extends ClientConnection implements GenericClientController {
    private FXClientController controller;

    FXClientConnection(FXClientController controller) throws IOException {
        super();
        this.controller = controller;
    }

    @Override
    public void displayMessage(ServerMessage message) {
        Platform.runLater(() -> controller.processServerMessage(message));
    }

    @Override
    public void displayString(String s) {
        Platform.runLater(() -> controller.displayMessage(s));
    }

    @Override
    public void displayQuestion(Question q) {
        Platform.runLater(() -> controller.displayQuestion(q));
    }

    @Override
    public void displayCurrentScores(Integer[] scores) {
        Platform.runLater(() -> controller.displayPoints(scores));
    }

    @Override
    public void displayScoreHistory(ArrayList<List<Integer>> scores) {
        List<Integer> playerOneHistory = scores.get(0);
        List<Integer> playerTwoHistory = scores.get(1);
        System.out.println(playerOneHistory);
        System.out.println(playerTwoHistory);
    }

    @Override
    public ClientConnection getConnection() {
        return this;
    }
}
