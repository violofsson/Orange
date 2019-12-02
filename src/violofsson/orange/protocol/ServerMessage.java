package violofsson.orange.protocol;

import java.io.Serializable;

public class ServerMessage implements Serializable {
    public enum Headers {
        WELCOME,
        WAIT,
        CHOOSE_CATEGORY,
        QUESTION,
        CORRECT_ANSWER,
        YOU_WIN,
        YOU_LOSE,
        YOU_TIED,
        CURRENT_SCORE,
        SCORE_HISTORY,
        UNDEFINED
    }

    private final Headers header;
    private Serializable embeddedObject;

    public ServerMessage(Headers header, String body) {
        this.header = header;
        this.embeddedObject = body;
    }

    public ServerMessage(Headers header, Serializable embeddedObject) {
        this.header = header;
        this.embeddedObject = embeddedObject;
    }

    public Integer[] decodeCurrentScores() throws Exception {
        if (this.header == Headers.CURRENT_SCORE
                && embeddedObject instanceof Integer[]) {
            return (Integer[]) embeddedObject;
        } else {
            throw new Exception();
        }
    }

    public Integer[][] decodeScoreHistory() throws Exception {
        if (this.header == Headers.SCORE_HISTORY
                && embeddedObject instanceof Integer[][]) {
            return (Integer[][]) embeddedObject;
        } else {
            throw new Exception();
        }
    }

    public String[] decodeStringArray() throws Exception {
        if (embeddedObject instanceof String[]) {
            return (String[]) embeddedObject;
        } else if (embeddedObject instanceof String) {
            return new String[]{(String) embeddedObject};
        } else {
            throw new Exception();
        }
    }

    public Headers getHeader() {
        return this.header;
    }

    public String getString() {
        // Fel om icke-sträng lagrats?
        return embeddedObject.toString();
    }
}
