package violofsson.orange.protocol;

import java.io.Serializable;

public class ServerMessage implements Serializable {
    public enum Headers {
        WELCOME,
        WAIT,
        CHOOSE_CATEGORY,
        YOU_WIN,
        YOU_LOSE,
        YOU_TIED,
        CURRENT_SCORE,
        SCORE_HISTORY
    }

    public final Headers HEADER;
    public final String MESSAGE;

    public ServerMessage(Headers header, String message) {
        this.HEADER = header;
        this.MESSAGE = message;
    }
}
