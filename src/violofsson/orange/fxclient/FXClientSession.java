package violofsson.orange.fxclient;

import javafx.application.Platform;
import violofsson.orange.protocol.Question;
import violofsson.orange.protocol.ServerMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class FXClientSession implements Runnable {
    private FXClientController controller;
    private Socket socket;
    private PrintWriter out;
    private ObjectInputStream in;

    FXClientSession(FXClientController controller) throws IOException {
        socket = new Socket("localhost", 56565);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new ObjectInputStream(socket.getInputStream());
        this.controller = controller;
    }

    @Override
    public void run() {
        // TODO Ta reda på varför Platform.runLater behövs
        try {
            while (socket.isConnected()) {
                Object obj;
                obj = receive();
                if (obj instanceof Question) {
                    Question question = (Question) obj;
                    Platform.runLater(() -> controller.displayQuestion(question));
                } else if (obj instanceof ServerMessage) {
                    ServerMessage fromServer = (ServerMessage) obj;
                    Platform.runLater(() -> controller.processServerMessage(fromServer));
                } else if (obj instanceof String) {
                    String message = (String) obj;
                    Platform.runLater(() -> controller.displayMessage(message));
                } else if (obj instanceof Integer[]) {
                    Integer[] points = (Integer[]) obj;
                    Platform.runLater(() -> controller.displayPoints(points));
                } else if (obj instanceof ArrayList) {
                    // Kontrollera typer!
                    ArrayList<List> lista = (ArrayList) obj;
                    List<Integer> playerOneHistory = lista.get(0);
                    List<Integer> playerTwoHistory = lista.get(1);
                    /*String playerOneText = getScoreSummary("Spelare 1",
                            playerOneHistory);
                    String playerTwoText = getScoreSummary("Spelare 2",
                            playerTwoHistory);
                    JOptionPane.showMessageDialog(this, playerOneText + "\n\n" + playerTwoText);*/
                    // TODO Ordna en riktig översikt
                    System.out.println(playerOneHistory.toString());
                    System.out.println(playerTwoHistory);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    Object receive() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    void send(Object obj) {
        out.println(obj);
    }
}
