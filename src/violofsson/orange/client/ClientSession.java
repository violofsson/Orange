package violofsson.orange.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSession {
    private Socket socket;
    private PrintWriter out;
    private ObjectInputStream in;

    ClientSession() throws IOException {
        socket = new Socket("localhost", 56565);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new ObjectInputStream(socket.getInputStream());
    }

    Object read() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    void write(Object obj) {
        out.println(obj);
    }
}
