package violofsson.orange.protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {
    private PrintWriter out;
    private ObjectInputStream in;

    public ClientConnection() throws IOException {
        Socket socket = new Socket("localhost", 56565);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Object receive() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public void send(Object obj) {
        out.println(obj);
    }
}
