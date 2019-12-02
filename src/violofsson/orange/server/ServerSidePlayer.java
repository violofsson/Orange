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
    List<Integer> scoreHistory = new ArrayList<>();

    private BufferedReader input;
    private ObjectOutputStream outputObject;

    ServerSidePlayer(Socket socket, String name) {
        this.name = name;
        try {
            outputObject = new ObjectOutputStream(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            sendMessage(ServerMessage.Headers.WELCOME, "Welcome " + name);
            sendMessage(ServerMessage.Headers.WAIT, "Wait until the other player is connected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String readLine() throws IOException {
        return input.readLine();
    }

    void sendMessage(ServerMessage message) throws IOException {
        outputObject.reset();
        outputObject.writeObject(message);
    }

    void sendMessage(ServerMessage.Headers header, String message) throws IOException {
        sendMessage(new ServerMessage(header, message));
    }

    <T extends Serializable> void sendArray(ServerMessage.Headers header, T[] array) throws IOException {
        sendMessage(new ServerMessage(header, array));
    }

    void sendQuestion(Question q) throws IOException {
        outputObject.reset();
        outputObject.writeObject(q);
    }
}
