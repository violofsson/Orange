package violofsson.orange.server;

import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ServerSidePlayer {
    int totPoints = 0;
    int questionNumber = 0;
    String name;
    List <Integer> scoreHistory = new ArrayList<>();

    private BufferedReader input;
    private ObjectOutputStream outputObject;

    ServerSidePlayer(Socket socket, String name) {
        this.name = name;
        try {
            outputObject = new ObjectOutputStream(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            outputObject.writeObject("Welcome: "+name);
            outputObject.writeObject("Wait until the other player is connected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String readLine() throws IOException {
        return input.readLine();
    }

    void sendMessage(ServerMessage.Headers header, String message) throws IOException {
        outputObject.reset();
        outputObject.writeObject(new ServerMessage(header, message));
    }

    void sendObject(Object obj) throws IOException {
        outputObject.reset();
        outputObject.writeObject(obj);
    }

    void sendQuestion(Question q) throws IOException {
        outputObject.reset();
        outputObject.writeObject(q);
    }
}
