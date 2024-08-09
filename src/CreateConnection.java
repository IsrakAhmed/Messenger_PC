import java.util.HashMap;

public class CreateConnection implements Runnable{
    
    HashMap<String, Information> clientList;
    NetworkConnection networkConnection;
    public CreateConnection(HashMap<String,Information> clientList, NetworkConnection networkConnection){
        this.clientList = clientList;
        this.networkConnection = networkConnection;
    }
        
    
    @Override
    public void run() {
        Object userObject = networkConnection.read();
        String userName = (String) userObject;
        
        System.out.println("User : " + userName + " Connected");
        
        clientList.put(userName, new Information(userName, networkConnection));

        System.out.println("HashMap updated" + clientList);

        new Thread(new ReaderWriterServer(userName, networkConnection, clientList)).start();
        
    }
    
}
