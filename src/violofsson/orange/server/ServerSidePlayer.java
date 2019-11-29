package violofsson.orange.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerSidePlayer {
    private ServerSidePlayer opponent;
    int totPoints = 0;
    int questionNumber = 0;
    String name;
    List <Integer> scoreHistory = new ArrayList<>();

    BufferedReader input;
    ObjectOutputStream outputObject;

    ServerSidePlayer(Socket socket, String name) {
        this.name = name;

        try {
            outputObject = new ObjectOutputStream(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputObject.writeObject("Welcome: "+name);
            outputObject.writeObject("Wait until the other player is connected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//Constructor

    ServerSidePlayer getOpponent() {
        return this.opponent;
    }

    void setOpponent(ServerSidePlayer opponent) {
        this.opponent = opponent;
    }
}
