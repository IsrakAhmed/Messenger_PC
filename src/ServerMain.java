import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerMain {

    private static final int TCP_PORT = 12345;
    private static final int BROADCAST_PORT = 8888;

    private static final String SERVER_NAME = "PC Server";

    public static void main(String[] args) {
        try {
            // Start broadcasting the server IP
            new Thread(() -> broadcastServerIp()).start();

            // Start TCP server
            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Server Started...");
            System.out.println("Server IP: " + InetAddress.getLocalHost().getHostAddress());

            // Map to store client information
            HashMap<String, Information> clientList = new HashMap<>();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());

                NetworkConnection networkConnection = new NetworkConnection(socket);
                new Thread(new CreateConnection(clientList, networkConnection)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void broadcastServerIp() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            String message = serverIp + "@" + SERVER_NAME; // Include server name in the message
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    InetAddress.getByName("255.255.255.255"),
                    BROADCAST_PORT
            );

            while (true) {
                socket.send(packet);
                System.out.println("Broadcasting server IP: " + serverIp);
                Thread.sleep(5000); // Broadcast every 5 seconds
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error broadcasting server IP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

