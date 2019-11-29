package violofsson.orange.server;

import com.google.gson.Gson;
import violofsson.orange.protocol.Question;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO Hantera nätfel och andra specialfall

public class Database {
    static class CategoryAPICall {
        List<CategoryAPIEntry> trivia_categories;
    }

    static class CategoryAPIEntry {
        int id;
        String name;
    }

    static class QuestionAPICall {
        List<SerializedQuestion> results;

        List<Question> getQuestions() {
            return results.stream().map(SerializedQuestion::toRealQuestion)
                    .collect(Collectors.toList());
        }
    }

    static class SerializedQuestion {
        String question;
        String correct_answer;
        List<String> incorrect_answers;

        Question toRealQuestion() {
            question = URLDecoder.decode(question, StandardCharsets.UTF_8);
            correct_answer = URLDecoder.decode(correct_answer,
                    StandardCharsets.UTF_8);
            for (int i = 0; i < incorrect_answers.size(); i++) {
                incorrect_answers.set(i, URLDecoder.decode(
                        incorrect_answers.get(i), StandardCharsets.UTF_8));
            }
            return new Question(question, correct_answer, incorrect_answers);
        }
    }

    /*class TokenResponse {
        private int response_code;
        private String response_message;
        private String token;
    }*/

    private Gson deserializer = new Gson();
    //private String apiToken;
    private Map<String, Integer> categoryIDs;

    public Database() {
        try {
            loadCategories();
            /*URL tokenRequestURL = new URL("https://opentdb.com/api_token.php?command=request");
            TokenResponse tr = deserializer.fromJson(
                    new InputStreamReader(
                            tokenRequestURL.openStream()),
                    TokenResponse.class);
            if (tr.response_code == 0)
                apiToken = tr.token;
            else
                throw new IOException();*/
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public List<String> getRandomCategories(int wantedCategories) {
        // TODO Tomma och/eller stora listor
        if (wantedCategories >= categoryIDs.size()) {
            wantedCategories = categoryIDs.size();
        }
        List<String> categories = new ArrayList<>(categoryIDs.keySet());
        Collections.shuffle(categories);
        return categories.subList(0, wantedCategories);
    }

    public List<Question> getQuestions(String wantedCategory,
                                       int numberOfQuestions) {
        try {
            int categoryId = categoryIDs.getOrDefault(wantedCategory, 9);
            URL questionRequest = new URL(
                    "https://opentdb.com/api.php?amount="
                            + numberOfQuestions
                            + "&category=" + categoryId
                            + "&encode=url3986"
                            + "&type=multiple"
                    /*+ "&token=" + apiToken*/);
            QuestionAPICall qr = deserializer.fromJson(
                    new InputStreamReader(questionRequest.openStream()),
                    QuestionAPICall.class);
            return qr.getQuestions();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private void loadCategories() throws IOException {
        URL categoryURL = new URL("https://opentdb.com/api_category.php");
        CategoryAPICall catCall = deserializer.fromJson(
                new InputStreamReader(categoryURL.openStream()),
                CategoryAPICall.class);
        categoryIDs = catCall.trivia_categories.stream()
                .collect(Collectors.toMap(
                        c -> URLDecoder.decode(c.name, StandardCharsets.UTF_8),
                        c -> c.id));
    }

    String getCategoryString() {
        return String.join(";", getRandomCategories(4));
    }
}
