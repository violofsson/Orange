package violofsson.orange.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerMessage implements Serializable {
    static final String DELIMITER=";";

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

    public ServerMessage(Headers header, String body) {
        this.header = header;
        this.body = body;
    }

    public static String[] parseCategories(String s) {
        return s.split(DELIMITER);
    }

    public static Integer[] parseCurrentScores(String s) {
        String[] array = s.split(DELIMITER);
        Integer[] scores = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            scores[i] = Integer.parseInt(array[i]);
        }
        return scores;
    }

    public static List<List<Integer>> parseScoreHistory(String s) {
        List<Integer> integers = Arrays.stream(s.split(DELIMITER))
                .mapToInt(Integer::parseInt).boxed()
                .collect(Collectors.toList());
        List<List<Integer>> result = new ArrayList<>();
        result.add(integers.subList(0, integers.size()/2));
        result.add(integers.subList(integers.size()/2, integers.size()));
        return result;
    }
}
