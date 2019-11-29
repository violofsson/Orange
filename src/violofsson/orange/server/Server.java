package violofsson.orange.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(56565);
        } catch (IOException e) {
            System.err.println("Server is not working");
            e.printStackTrace();
        }

        Properties p = new Properties();
        try {
            p.load(new FileInputStream("properties/testing.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int questionsPerRound = Integer.parseInt(p.getProperty("questions", "2"));
        int totalRounds = Integer.parseInt(p.getProperty("rounds", "2"));

        while (true) {
            ServerSideGame game = new ServerSideGame(questionsPerRound, totalRounds);

            ServerSidePlayer player1 = new ServerSidePlayer(listener.accept(), "Player 1");
            ServerSidePlayer player2 = new ServerSidePlayer(listener.accept(), "Player 2");

            game.setPlayers(player1, player2);
            game.start();
        }
    }
}
