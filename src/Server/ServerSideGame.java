package Server;

import Database.Database;

import java.io.IOException;

import question.Question;

import java.util.List;


public class ServerSideGame extends Thread {

    Database db = new Database();
    ServerSidePlayer currentPlayer;
    private List<Question> questions;
    private int questionsPerRound;
    private int totalRounds;
    private int currentRound = 0;

    private static final int SELECTING_CATEGORY = 0;
    private static final int ASKING_QUESTIONS = 1;
    private static final int SWITCH_PLAYER = 2;
    private static final int ALL_QUESTIONS_ANSWERED = 3;
    int currentState = SELECTING_CATEGORY;

    @Override
    public void run() {
        try {

            while (true) {

                if (currentState == SELECTING_CATEGORY) {
                    currentPlayer.oponentPlayer.outputObject.writeObject("Wait until other player chooses a category!");
                    choosingCategory();
                    currentState = ASKING_QUESTIONS;
                    currentPlayer.oponentPlayer.outputObject.writeObject("Wait until other player answer");
                } else if (currentState == ASKING_QUESTIONS) {
                    handleQuestions();
                    currentState = SWITCH_PLAYER;
                } else if (currentState == SWITCH_PLAYER) {
                    switchingPlayer();
                } else if (currentState == ALL_QUESTIONS_ANSWERED) {
                    sendPoints();
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
            currentPlayer.oponentPlayer.totPoints = 0;
            currentRound = currentRound % totalRounds;
            sendPoints();
            db.resetCount();
            db.shuffleLists();
        }
    }

    private void sendPoints() throws IOException {
        if (currentPlayer.name.equalsIgnoreCase("Player 1")) { // Ändra till currentPlayer.id ?
            Integer[] points = {currentPlayer.totPoints, currentPlayer.oponentPlayer.totPoints};
            currentPlayer.outputObject.writeObject(points);
            currentPlayer.oponentPlayer.outputObject.writeObject(points);
            currentState = SELECTING_CATEGORY;
        } else {
            Integer[] points = {currentPlayer.oponentPlayer.totPoints, currentPlayer.totPoints};
            currentPlayer.oponentPlayer.outputObject.writeObject(points);
            currentPlayer.outputObject.writeObject(points);
            currentState = SELECTING_CATEGORY;
        }
    }

    private void switchingPlayer() throws IOException {
        if (isRoundOver()) {
            System.out.println(questions.size());
            currentState = ALL_QUESTIONS_ANSWERED;
            System.out.println(questions.size());
        } else {
            switchPlayer();
            currentPlayer.oponentPlayer.outputObject
                    .writeObject("Wait for the opponent");
            currentState = ASKING_QUESTIONS;
        }
    }


    private void choosingCategory() throws IOException {
        currentPlayer.outputObject.writeObject("Choose category :");
        String category = currentPlayer.input.readLine();
        currentPlayer.game.selectCatagory(category);
    }

    private void handleQuestions() throws IOException {
        Question q;
        while (!allQuestionsAnswered()) {
            q = questions.get(currentPlayer.questionNumber);
            currentPlayer.outputObject.writeObject(q);
            String answer = currentPlayer.input.readLine();

            if (q.isRightAnswer(answer)) {
                currentPlayer.totPoints++;
            }
            currentPlayer.game.nextQuestion();// index ökar med 1
        }//while
    }//handleQuestions


    ServerSideGame(int questionsPerRound, int totalRounds) {
        this.questionsPerRound = questionsPerRound;
        this.totalRounds = totalRounds;
    }

    public void hasWinner() throws IOException {
        if (isGameOver()) {
            if (currentPlayer.totPoints > currentPlayer.oponentPlayer.totPoints) {
                currentPlayer.outputObject.writeObject("YOU WIN");
                currentPlayer.oponentPlayer.outputObject.writeObject("YOU LOSE");
            } else if (currentPlayer.totPoints < currentPlayer.oponentPlayer.totPoints) {
                currentPlayer.outputObject.writeObject("YOU LOSE");
                currentPlayer.oponentPlayer.outputObject.writeObject("YOU WIN");
            } else {
                currentPlayer.outputObject.writeObject("YOU TIED");
                currentPlayer.oponentPlayer.outputObject.writeObject("YOU TIED");
            }
        }
    }

    public synchronized boolean isTie() {
        if (isGameOver()) {
            if (currentPlayer.totPoints == currentPlayer.oponentPlayer.totPoints) {
                return true;
            }
        }
        return false;
    }


    public synchronized boolean isRoundOver() {
        if (currentPlayer.questionNumber == questionsPerRound
                && currentPlayer.oponentPlayer.questionNumber == questionsPerRound) {
            currentPlayer.questionNumber = 0; // nollställer om rundan är över (Problemet är att det finns risk för
            //  att man kan få samma fråga igen om man väljer samma kategori)
            // en annan lösning är att man endast nollställer om questionNumber når list.size()
            currentPlayer.oponentPlayer.questionNumber = 0;
            ///currentRound++; ökar i selectCategory
            return true;
        } else {
            return false;
        }
    }


    public synchronized boolean isGameOver() {
        if (currentRound == totalRounds) {
            //currentRound = 0; // nollställa currentRound???
            return true;
        }
        return false;
    }


    public synchronized void switchPlayer() {
        currentPlayer = currentPlayer.oponentPlayer;

    }


    public synchronized boolean allQuestionsAnswered() {
        return currentPlayer.questionNumber == questionsPerRound;
    }


    public synchronized void nextQuestion() {
        currentPlayer.questionNumber++;
    }


    public synchronized void selectCatagory(String categoryName) {
        questions = db.getQuestions(categoryName, questionsPerRound);
        currentRound++;
    }

    public List<Question> getQuestions() {
        return questions;
    }


}