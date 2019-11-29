package violofsson.orange.server;

import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerSideGame extends Thread {
    private enum States {
        SELECTING_CATEGORY,
        ASKING_QUESTIONS,
        SWITCH_PLAYER,
        ALL_QUESTIONS_ANSWERED
    }

    private Database db = new Database();
    private ServerSidePlayer currentPlayer;
    private List<Question> questions;
    private int questionsPerRound;
    private int totalRounds;
    private int currentRound = 0;
    private States currentState = States.SELECTING_CATEGORY;

    ServerSideGame(int questionsPerRound, int totalRounds) {
        this.questionsPerRound = questionsPerRound;
        this.totalRounds = totalRounds;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (currentState == States.SELECTING_CATEGORY) {
                    currentPlayer.getOpponent().sendMessage(
                            ServerMessage.Headers.WAIT,
                            "Wait until other player chooses a category!");
                    choosingCategory();
                    currentState = States.ASKING_QUESTIONS;
                    currentPlayer.getOpponent().sendMessage(
                            ServerMessage.Headers.WAIT,
                            "Wait until other player answer");
                } else if (currentState == States.ASKING_QUESTIONS) {
                    handleQuestions();
                    currentState = States.SWITCH_PLAYER;
                } else if (currentState == States.SWITCH_PLAYER) {
                    switchingPlayer();
                } else if (currentState == States.ALL_QUESTIONS_ANSWERED) {
                    sendPoints();
                    sendPointsHistory();
                    hasWinner();
                    resetGame();
                }
            }//While
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//run

    private void resetGame() throws IOException {
        if (isGameOver()) {
            currentPlayer.totPoints = 0;
            currentPlayer.getOpponent().totPoints = 0;
            currentRound = currentRound % totalRounds;
            sendPoints();
        }
    }

    void setPlayers(ServerSidePlayer playerOne, ServerSidePlayer playerTwo) {
        playerOne.setOpponent(playerTwo);
        playerTwo.setOpponent(playerOne);
        currentPlayer = playerOne;
    }

    private ServerSidePlayer getPlayerOne() {
        if (currentPlayer.name.equalsIgnoreCase("Player 1")) {
            return currentPlayer;
        } else {
            return currentPlayer.getOpponent();
        }
    }

    private ServerSidePlayer getPlayerTwo() {
        return getPlayerOne().getOpponent();
    }

    private void sendPointsHistory() throws IOException {
        ArrayList<List> histories = new ArrayList<>();
        histories.add(getPlayerOne().scoreHistory);
        histories.add(getPlayerTwo().scoreHistory);

        getPlayerOne().sendObject(histories);
        getPlayerTwo().sendObject(histories);
    }

    private void sendPoints() throws IOException {
        Integer[] points = {getPlayerOne().totPoints, getPlayerTwo().totPoints};
        getPlayerOne().sendObject(points);
        getPlayerTwo().sendObject(points);
        currentState = States.SELECTING_CATEGORY;
    }

    private void switchingPlayer() throws IOException {
        if (isRoundOver()) {
            currentState = States.ALL_QUESTIONS_ANSWERED;
        } else {
            switchPlayer();
            currentPlayer.getOpponent().sendMessage(ServerMessage.Headers.WAIT,
                    "Wait for the opponent");
            currentState = States.ASKING_QUESTIONS;
        }
    }

    private void choosingCategory() throws IOException {
        currentPlayer.sendMessage(ServerMessage.Headers.CHOOSE_CATEGORY,
                db.getCategoryString());
        String category = currentPlayer.readLine();
        selectCategory(category);
    }

    private void handleQuestions() throws IOException {
        Question q;
        int tempScore = 0;
        while (!allQuestionsAnswered()) {
            q = questions.get(currentPlayer.questionNumber);
            currentPlayer.sendQuestion(q);
            String answer = currentPlayer.readLine();

            if (q.isRightAnswer(answer)) {
                currentPlayer.totPoints++;
                tempScore++;
            }
            nextQuestion();// index ökar med 1
        }
        currentPlayer.scoreHistory.add(tempScore);
    }

    private void hasWinner() throws IOException {
        if (isGameOver()) {
            if (currentPlayer.totPoints > currentPlayer.getOpponent().totPoints) {
                currentPlayer.sendMessage(
                        ServerMessage.Headers.YOU_WIN, "YOU WIN");
                currentPlayer.getOpponent().sendMessage(
                        ServerMessage.Headers.YOU_LOSE, "YOU LOSE");
            } else if (currentPlayer.totPoints < currentPlayer.getOpponent().totPoints) {
                currentPlayer.sendMessage(
                        ServerMessage.Headers.YOU_LOSE, "YOU LOSE");
                currentPlayer.getOpponent().sendMessage(
                        ServerMessage.Headers.YOU_WIN, "YOU WIN");
            } else {
                currentPlayer.sendMessage(
                        ServerMessage.Headers.YOU_TIED, "YOU TIED");
                currentPlayer.getOpponent().sendMessage(
                        ServerMessage.Headers.YOU_TIED, "YOU TIED");
            }
        }
    }

    private synchronized boolean isRoundOver() {
        if (currentPlayer.questionNumber == questionsPerRound
                && currentPlayer.getOpponent().questionNumber == questionsPerRound) {
            // nollställer om rundan är över (Problemet är att det finns risk för
            //  att man kan få samma fråga igen om man väljer samma kategori)
            // en annan lösning är att man endast nollställer om questionNumber når list.size()
            currentPlayer.questionNumber = 0;
            currentPlayer.getOpponent().questionNumber = 0;
            return true;
        } else {
            return false;
        }
    }

    private synchronized boolean isGameOver() {
        return currentRound == totalRounds;
    }

    private synchronized void switchPlayer() {
        currentPlayer = currentPlayer.getOpponent();
    }

    private synchronized boolean allQuestionsAnswered() {
        return currentPlayer.questionNumber == questionsPerRound;
    }

    private synchronized void nextQuestion() {
        currentPlayer.questionNumber++;
    }

    private synchronized void selectCategory(String categoryName) {
        questions = db.getQuestions(categoryName, questionsPerRound);
        currentRound++;
    }
}
