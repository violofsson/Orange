package violofsson.orange.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question implements Serializable {
    static final long serialVersionUID = 42L;
    private String question;
    private String rightAnswer;
    private List<String> alternatives = new ArrayList<>();

    public Question(String question, String rightAnswer, List<String> wrongAnswers) {
        this.question = question;
        this.rightAnswer = rightAnswer;
        alternatives.add(rightAnswer);
        alternatives.addAll(wrongAnswers);
        Collections.shuffle(alternatives);
    }

    public List<String> getAlternatives() {
        return alternatives;
    }

    public String getQuestion() {
        return question;
    }

    public boolean isRightAnswer(String s) {
        return s.equals(rightAnswer);
    }
}
