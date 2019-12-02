package violofsson.orange.server;

import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import java.io.IOException;

import static violofsson.orange.protocol.ServerMessage.*;

public class ServerSideGame extends Thread {
    private enum States {
        SELECTING_CATEGORY,
        ASKING_QUESTIONS,
        SWITCH_PLAYER,
        ALL_QUESTIONS_ANSWERED
    }

    private Database db = new Database();
    private ServerSidePlayer playerOne, playerTwo, currentPlayer;
    private Question[] questions;
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
                    currentOpponent().sendMessage(Headers.WAIT,
                            "Wait until other player chooses a category!");
                    selectNewCategory();
                    currentState = States.ASKING_QUESTIONS;
                    currentOpponent().sendMessage(Headers.WAIT,
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
                    // TODO Avsluta spel i stället för att starta om?
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
            q = questions[currentPlayer.questionNumber];
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
                playerOne.sendMessage(Headers.YOU_WIN, "YOU WIN");
                playerTwo.sendMessage(Headers.YOU_LOSE, "YOU LOSE");
            } else if (playerOne.totPoints < playerTwo.totPoints) {
                playerOne.sendMessage(Headers.YOU_LOSE, "YOU LOSE");
                playerTwo.sendMessage(Headers.YOU_WIN, "YOU WIN");
            } else {
                playerOne.sendMessage(Headers.YOU_TIED, "YOU TIED");
                playerTwo.sendMessage(Headers.YOU_TIED, "YOU TIED");
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
        currentPlayer.sendArray(Headers.CHOOSE_CATEGORY,
                db.getRandomCategories(4).toArray(new String[4]));
        String selectedCategory = currentPlayer.readLine();
        questions = db.getQuestions(selectedCategory, questionsPerRound);
        currentRound++;
    }

    private synchronized void sendScore() throws IOException {
        ServerMessage msg = new ServerMessage(Headers.CURRENT_SCORE,
                new Integer[]{playerOne.totPoints, playerTwo.totPoints});
        playerOne.sendMessage(msg);
        playerTwo.sendMessage(msg);
        currentState = States.SELECTING_CATEGORY;
    }

    private synchronized void sendScoreHistory() throws IOException {
        Integer[][] scoreHistories = {
                playerOne.scoreHistory.toArray(new Integer[0]),
                playerTwo.scoreHistory.toArray(new Integer[0])
        };
        playerOne.sendArray(Headers.SCORE_HISTORY, scoreHistories);
        playerTwo.sendArray(Headers.SCORE_HISTORY, scoreHistories);
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
            currentPlayer.sendMessage(Headers.WAIT,
                    "Wait for the opponent");
            currentPlayer = currentOpponent();
            currentState = States.ASKING_QUESTIONS;
        }
    }
}
