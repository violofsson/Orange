package violofsson.orange.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerMessage implements Serializable {
    public enum Headers {
        WELCOME,
        WAIT,
        CHOOSE_CATEGORY,
        QUESTION,
        YOU_WIN,
        YOU_LOSE,
        YOU_TIED,
        CURRENT_SCORE,
        SCORE_HISTORY,
        UNDEFINED
    }

    public final Headers header;
    public final String body;
    static final String DELIMITER=";";

    public ServerMessage(Headers header, String body) {
        this.header = header;
        this.body = body;
    }

    public static Integer[] decodeCurrentScores(String s) {
        String[] array = s.split(DELIMITER);
        Integer[] scores = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            scores[i] = Integer.parseInt(array[i]);
        }
        return scores;
    }

    public static List<List<Integer>> decodeScoreHistory(String s) {
        List<Integer> integers = Arrays.stream(s.split(DELIMITER))
                .mapToInt(Integer::parseInt).boxed()
                .collect(Collectors.toList());
        List<List<Integer>> result = new ArrayList<>();
        result.add(integers.subList(0, integers.size()/2));
        result.add(integers.subList(integers.size()/2, integers.size()));
        return result;
    }

    public static String[] decodeStringList(String s) {
        return s.split(DELIMITER);
    }

    public static String encodeCurrentScores(int playerOne, int playerTwo) {
        return playerOne + DELIMITER + playerTwo;
    }

    public static String encodeScoreHistories(List<Integer> playerOne, List<Integer> playerTwo) {
        return Stream.concat(playerOne.stream(), playerTwo.stream())
                .map(Object::toString)
                .collect(Collectors.joining(ServerMessage.DELIMITER));
    }

    public static String encodeStringList(List<String> list) {
        return String.join(DELIMITER, list);
    }
}
