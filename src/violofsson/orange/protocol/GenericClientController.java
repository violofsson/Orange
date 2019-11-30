package violofsson.orange.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface GenericClientController extends Runnable {
    void displayMessage(ServerMessage message);
    void displayMessage(String s);
    void displayQuestion(Question q);
    void displayScores(Integer[] scores);
    void displayScores(ArrayList<List<Integer>> scores);
    ClientConnection getConnection();

    default void run() {
        Object obj;
        try {
            while ((obj = getConnection().receive()) != null) {
                if (obj instanceof Question) {
                    Question question = (Question) obj;
                    displayQuestion(question);
                } else if (obj instanceof ServerMessage) {
                    ServerMessage fromServer = (ServerMessage) obj;
                    displayMessage(fromServer);
                } else if (obj instanceof String) {
                    String message = (String) obj;
                    displayMessage(message);
                } else if (obj instanceof Integer[]) {
                    Integer[] points = (Integer[]) obj;
                    displayScores(points);
                } else if (obj instanceof ArrayList) {
                    ArrayList<List<Integer>> lista = (ArrayList<List<Integer>>) obj;
                    displayScores(lista);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
