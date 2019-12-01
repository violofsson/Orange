package violofsson.orange.server;

import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import java.io.IOException;
import java.util.List;

public class ServerSideGame extends Thread {
    private enum States {
        SELECTING_CATEGORY,
        ASKING_QUESTIONS,
        SWITCH_PLAYER,
        ALL_QUESTIONS_ANSWERED
    }

    private Database db = new Database();
    private ServerSidePlayer playerOne, playerTwo, currentPlayer;
    private List<Question> questions;
    private int questionsPerRound, totalRounds;
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
                    currentOpponent().sendMessage(
                            ServerMessage.Headers.WAIT,
                            "Wait until other player chooses a category!");
                    selectNewCategory();
                    currentState = States.ASKING_QUESTIONS;
                    currentOpponent().sendMessage(
                            ServerMessage.Headers.WAIT,
                            "Wait until other player answer");
                } else if (currentState == States.ASKING_QUESTIONS) {
                    handleQuestions();
                    currentState = States.SWITCH_PLAYER;
                } else if (currentState == States.SWITCH_PLAYER) {
                    switchPlayer();
                } else if (currentState == States.ALL_QUESTIONS_ANSWERED) {
                    sendScore();
                    sendScoreHistory();
                    hasWinner();
                    resetGame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO Avgör vilka metoder som faktiskt behöver vara synchronized

    private boolean allQuestionsAnswered() {
        return currentPlayer.questionNumber == questionsPerRound;
    }

    private ServerSidePlayer currentOpponent() {
        return (currentPlayer == playerOne) ? playerTwo : playerOne;
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
            nextQuestion();
        }
        currentPlayer.scoreHistory.add(tempScore);
    }

    private synchronized void hasWinner() throws IOException {
        if (isGameOver()) {
            if (playerOne.totPoints > playerTwo.totPoints) {
                playerOne.sendMessage(
                        ServerMessage.Headers.YOU_WIN, "YOU WIN");
                playerTwo.sendMessage(
                        ServerMessage.Headers.YOU_LOSE, "YOU LOSE");
            } else if (playerOne.totPoints < playerTwo.totPoints) {
                playerOne.sendMessage(
                        ServerMessage.Headers.YOU_LOSE, "YOU LOSE");
                playerTwo.sendMessage(
                        ServerMessage.Headers.YOU_WIN, "YOU WIN");
            } else {
                playerOne.sendMessage(
                        ServerMessage.Headers.YOU_TIED, "YOU TIED");
                playerTwo.sendMessage(
                        ServerMessage.Headers.YOU_TIED, "YOU TIED");
            }
        }
    }

    private boolean isGameOver() {
        return currentRound == totalRounds;
    }

    private boolean isRoundOver() {
        if (playerOne.questionNumber == questionsPerRound
                && playerTwo.questionNumber == questionsPerRound) {
            playerOne.questionNumber = 0;
            playerTwo.questionNumber = 0;
            return true;
        } else {
            return false;
        }
    }

    private void nextQuestion() {
        currentPlayer.questionNumber++;
    }

    private synchronized void resetGame() throws IOException {
        if (isGameOver()) {
            playerOne.totPoints = 0;
            playerTwo.totPoints = 0;
            currentRound = currentRound % totalRounds;
            sendScore();
        }
    }

    private synchronized void selectNewCategory() throws IOException {
        currentPlayer.sendMessage(ServerMessage.Headers.CHOOSE_CATEGORY,
                ServerMessage.encodeStringList(db.getRandomCategories(4)));
        String selectedCategory = currentPlayer.readLine();
        questions = db.getQuestions(selectedCategory, questionsPerRound);
        currentRound++;
    }

    private synchronized void sendScore() throws IOException {
        ServerMessage msg = new ServerMessage(
                ServerMessage.Headers.CURRENT_SCORE,
                ServerMessage.encodeCurrentScores(
                        playerOne.totPoints, playerTwo.totPoints));
        playerOne.sendMessage(msg);
        playerTwo.sendMessage(msg);
        currentState = States.SELECTING_CATEGORY;
    }

    private synchronized void sendScoreHistory() throws IOException {
        ServerMessage msg = new ServerMessage(
                ServerMessage.Headers.SCORE_HISTORY,
                ServerMessage.encodeScoreHistories(
                        playerOne.scoreHistory, playerTwo.scoreHistory));
        playerOne.sendMessage(msg);
        playerTwo.sendMessage(msg);
    }

    void setPlayers(ServerSidePlayer playerOne, ServerSidePlayer playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        currentPlayer = playerOne;
    }

    private synchronized void switchPlayer() throws IOException {
        if (isRoundOver()) {
            currentState = States.ALL_QUESTIONS_ANSWERED;
        } else {
            currentPlayer.sendMessage(ServerMessage.Headers.WAIT,
                    "Wait for the opponent");
            currentPlayer = currentOpponent();
            currentState = States.ASKING_QUESTIONS;
        }
    }
}
