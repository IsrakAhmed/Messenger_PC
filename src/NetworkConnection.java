import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class NetworkConnection {
    Socket socket;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    public NetworkConnection(Socket socket) throws IOException {
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public NetworkConnection(String ip, int port) throws IOException{
        socket = new Socket(ip, port);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void write(Object obj){
        try {
            objectOutputStream.writeObject(obj);
        } catch (IOException ex) {
            System.out.println("Failed to write");
            //throw ex;
        }
    }

    public Object read(){
        Object object;
        try {
            object = objectInputStream.readObject();
        } catch (Exception ex) {
            System.out.println("Failed to read");
            return null;
        }
        return object;
    }

    public Socket getSocket() {
        return socket;
    }
}