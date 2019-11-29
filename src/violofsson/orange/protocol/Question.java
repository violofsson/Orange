package violofsson.orange.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Question implements Serializable {
    static final long serialVersionUID = 42L;
    private String question;
    private String rightAnswer;
    public ArrayList<String> alternatives = new ArrayList<>();

    public ArrayList<String> getAlternatives() {
        Collections.shuffle(alternatives);
        return alternatives;
    }

    public String getQuestion() {
        return question;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setRightAnswer(String rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    public boolean isRightAnswer(String s) {
        return s.equals(rightAnswer);
    }
}
