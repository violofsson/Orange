package violofsson.orange.fxclient;

import javafx.application.Platform;
import violofsson.orange.protocol.*;

import java.io.IOException;
import java.util.List;

class FXClientConnection extends ClientConnection implements GenericClientController {
    private FXClientController controller;

    FXClientConnection(FXClientController controller) throws IOException {
        super();
        this.controller = controller;
    }

    @Override
    public void displayCategories(String[] categories) {
        Platform.runLater(() -> controller.presentCategories(categories));
    }

    @Override
    public void displayServerMessage(ServerMessage message) {
        Platform.runLater(() -> controller.displayMessage(message.body));
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
    public void displayScoreHistory(List<List<Integer>> scores) {
        List<Integer> playerOneHistory = scores.get(0);
        List<Integer> playerTwoHistory = scores.get(1);
        System.out.println(playerOneHistory);
        System.out.println(playerTwoHistory);
    }

    @Override
    public void displayWinLossTie(ServerMessage message) {
        Platform.runLater(() -> controller.showEndMessage(message));
    }

    @Override
    public ClientConnection getConnection() {
        return this;
    }

    @Override
    public void setWaiting(String waitingMessage) {
        Platform.runLater(() -> controller.wait(waitingMessage));
    }

    @Override
    public void welcomePlayer(String welcomeMessage) {
        Platform.runLater(() -> controller.displayWelcome(welcomeMessage));
    }
}
