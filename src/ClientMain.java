import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class ClientMain {
    private static final int DISCOVERY_PORT = 8888; // The port for UDP broadcast
    private static final int SERVER_PORT = 12345; // The port for TCP connection
    private static String serverIp = null;
    private static String serverName = null;

    public static void main(String[] args) {
        try {
            // Discover server IP via UDP broadcast

           /* String serverIp = discoverServerIp();
            if (serverIp == null) {
                System.out.println("Server not found.");
                return;
            }*/

            HashMap<String, String> servers = discoverServerIPs();

            if (servers.isEmpty()) {
                System.out.println("No servers found.");
                return;
            }

            System.out.println("Select Server To Connect.............");
            System.out.print("Enter Server IP : ");

            Scanner in = new Scanner(System.in);

            String selectedServerIP = in.next();

            for (Map.Entry<String, String> entry : servers.entrySet()) {

                String ip = entry.getKey();
                String name = entry.getValue();

                if (Objects.equals(selectedServerIP, ip)) {
                    System.out.println("Connecting to server: " + name + " at IP: " + ip);
                    serverIp = ip;
                    serverName = name;
                    break;
                }
            }

            NetworkConnection networkConnection = new NetworkConnection(serverIp,SERVER_PORT);

            System.out.println("Enter your username");

            String userName = in.next();

            networkConnection.write(userName);

            Thread readerThread = new Thread(new Reader(networkConnection));
            Thread writerThread = new Thread(new Writer(networkConnection));

            readerThread.start();
            writerThread.start();

            try{
                readerThread.join();
                writerThread.join();
            }
            catch(Exception e){
                System.out.println("Thread exited");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> discoverServerIPs() {

        HashMap<String, String> servers = new HashMap<>();
        long startTime = System.currentTimeMillis();
        long timeoutPeriod = 15000; // 30 seconds timeout period
        long numberOfServers = 0;


        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            socket.setSoTimeout(5000); // Timeout for receiving the broadcast

            byte[] buffer = new byte[256];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Listening for server broadcasts...");

            while (System.currentTimeMillis() - startTime < timeoutPeriod) {
                try {
                    socket.receive(packet); // Receive the broadcast packet

                    String message = new String(packet.getData(), 0, packet.getLength()).trim();

                    String[] serverInfo = message.split("@"); // Split the message into server name and IP

                    String serverIP = serverInfo[0];
                    String serverName = serverInfo[1];

                    if (!servers.containsKey(serverIP)) {
                        servers.put(serverIP, serverName);

                        numberOfServers++;

                        startTime = System.currentTimeMillis(); // Reset the start time on receiving a packet to prevent timeout

                        System.out.println("Server Discovered :: " + "Server IP : " + serverIP + ", Name: " + serverName);
                    }

                } catch (IOException e) {
                    break; // Exit the loop when timeout occurs
                }
            }

        } catch (IOException e) {
            System.out.println("Failed to receive server broadcast.");
            e.printStackTrace();
        }

        return servers;
    }


    /*private static HashMap<String, String> discoverServerIPs() throws UnknownHostException {

        HashMap<String, String> servers = new HashMap<>();

        String ipAddressString = InetAddress.getLocalHost().getHostAddress();
        String subnet = ipAddressString.substring(0, ipAddressString.lastIndexOf(".") + 1);


        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int i = 1; i < 255; i++) {
            final String ip = subnet + i;

            int finalI = i;
            executor.execute(() -> {
                if (isDeviceUp(ip) && isServer(ip)) {
                    System.out.println("Found device at IP: " + ip);

                    servers.put(ip, "Server " + finalI);
                }
            });
        }
        executor.shutdown();

        return servers;
    }

    private static boolean isDeviceUp(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.isReachable(500); // Reduced timeout
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
        return false;
    }

    private static boolean isServer(String ipAddress) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, SERVER_PORT), 500); // Reduced timeout
            return true;
        } catch (IOException e) {
            return false;
        }
    }*/
}
