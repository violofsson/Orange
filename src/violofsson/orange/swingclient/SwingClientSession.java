package violofsson.orange.swingclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SwingClientSession {
    private Socket socket;
    private PrintWriter out;
    private ObjectInputStream in;

    SwingClientSession() throws IOException {
        socket = new Socket("localhost", 56565);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new ObjectInputStream(socket.getInputStream());
    }

    Object receive() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    void send(Object obj) {
        out.println(obj);
    }
}
