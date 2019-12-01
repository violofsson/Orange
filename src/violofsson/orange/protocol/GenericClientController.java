package violofsson.orange.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static violofsson.orange.protocol.ServerMessage.Headers.*;

public interface GenericClientController extends Runnable {
    void displayCategories(String[] categories);

    void displayServerMessage(ServerMessage message);

    void displayString(String s);

    void displayQuestion(Question q);

    void displayCurrentScores(Integer[] scores);

    void displayScoreHistory(List<List<Integer>> scores);

    void displayWinLossTie(ServerMessage message);

    ClientConnection getConnection();

    void setWaiting(String waitMessage);

    void welcomePlayer(String welcomeMessage);

    // TODO Låt controllern sköta all logik
    default void processServerMessage(ServerMessage message) {
        if (message.header == WELCOME) {
            welcomePlayer(message.body);
        } else if (message.header == WAIT) {
            setWaiting(message.body);
        } else if (message.header == CHOOSE_CATEGORY) {
            displayCategories(ServerMessage.parseCategories(message.body));
            /*} else if (message.header == QUESTION) {

             */
        } else if (message.header == YOU_WIN
                || message.header == YOU_LOSE
                || message.header == YOU_TIED) {
            displayWinLossTie(message);
        } else if (message.header == CURRENT_SCORE) {
            displayCurrentScores(ServerMessage.parseCurrentScores(message.body));
        } else if (message.header == SCORE_HISTORY) {
            displayScoreHistory(ServerMessage.parseScoreHistory(message.body));
        } else {
            displayServerMessage(message);
        }
    }

    @Override
    default void run() {
        Object obj;
        try {
            while ((obj = getConnection().receive()) != null) {
                if (obj instanceof Question) {
                    Question question = (Question) obj;
                    displayQuestion(question);
                } else if (obj instanceof ServerMessage) {
                    ServerMessage fromServer = (ServerMessage) obj;
                    processServerMessage(fromServer);
                } else if (obj instanceof String) {
                    String message = (String) obj;
                    displayString(message);
                } else if (obj instanceof Integer[]) {
                    Integer[] points = (Integer[]) obj;
                    displayCurrentScores(points);
                } else if (obj instanceof ArrayList) {
                    ArrayList<List<Integer>> lista = (ArrayList<List<Integer>>) obj;
                    displayScoreHistory(lista);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
