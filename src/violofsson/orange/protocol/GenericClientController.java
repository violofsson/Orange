package violofsson.orange.protocol;

import static violofsson.orange.protocol.ServerMessage.Headers.*;

public interface GenericClientController extends Runnable {
    void displayCategories(String[] categories);

    void displayServerMessage(ServerMessage message);

    void displayString(String s);

    void displayQuestion(Question q);

    void displayCurrentScores(Integer[] scores);

    void displayScoreHistory(Integer[][] scores);

    void displayWinLossTie(ServerMessage message);

    ClientConnection getConnection();

    void setWaiting(String waitMessage);

    void welcomePlayer(String welcomeMessage);

    // TODO Låt controllern sköta all logik
    default void processServerMessage(ServerMessage message) throws Exception {
        if (message.getHeader() == WELCOME) {
            welcomePlayer(message.getString());
        } else if (message.getHeader() == WAIT) {
            setWaiting(message.getString());
        } else if (message.getHeader() == CHOOSE_CATEGORY) {
            displayCategories(message.decodeStringArray());
            // TODO Hantera frågor och korrekta svar som ServerMessages
            /*} else if (message.getHeader() == QUESTION) {

             ] else if (message.getHeader() == CORRECT_ANSWER) {

             */
        } else if (message.getHeader() == YOU_WIN
                || message.getHeader() == YOU_LOSE
                || message.getHeader() == YOU_TIED) {
            displayWinLossTie(message);
        } else if (message.getHeader() == CURRENT_SCORE) {
            displayCurrentScores(message.decodeCurrentScores());
        } else if (message.getHeader() == SCORE_HISTORY) {
            displayScoreHistory(message.decodeScoreHistory());
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
                } else {
                    throw new Exception(obj.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
