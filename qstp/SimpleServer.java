package qstp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Vinay
 */

public class SimpleServer {
    
    static ArrayList<SimpleClientConnection> listOfClients;
    static ServerMessageQueue messageQueue;
    static boolean running;
    
    public static void main(String args[]) {
        try{
        ServerSocket server = new ServerSocket(8888);
        listOfClients = new ArrayList<SimpleClientConnection>();
        running=true;
        messageQueue = new ServerMessageQueue();
        messageQueue.start();
        long currentId = 0;
        System.out.println("Server listening for connections!");
        do {
            Socket s = server.accept();
            System.out.println("New connection! ID: " + currentId);
            sendToAll(new Message("Server", "New connection! ID: " + currentId + ". Say Hi!"));
            SimpleClientConnection newclient = new SimpleClientConnection(currentId++, s);
            newclient.start();            
            listOfClients.add(newclient);
            sendMessageToClient(new Message("Server", "Welcome to the server. Your ID is: " + (currentId-1), newclient));
            System.out.println("Number of currently connected clients: "+listOfClients.size());
        } while (listOfClients.size()>0);  //for successfully closing when no clients remain connected, need to run the acceptor in a different thread, perhaps use stop(). (to do)
        } catch (Exception e) { e.printStackTrace(); }
        System.exit(0);
    }
/*public static void removeClientId(long Id) {
    listOfClients.removeElement(Id);
}*/
public static void sendToAll(Message msg) { //requires only sender and messageText.
    for(SimpleClientConnection client: SimpleServer.listOfClients) { 
        msg.destinationSCC=client;
        SimpleServer.messageQueue.addMessageToQueue(msg);
    }
}
public static void sendMessageToClient(Message msg) { //requires sender, messageText, and destSCC
        SimpleServer.messageQueue.addMessageToQueue(msg);
}

public static void removeClient(SimpleClientConnection cc) {
    SimpleServer.listOfClients.remove(cc);
    SimpleServer.sendToAll(new Message("Server", "Client ID: " + cc.mId + " - connection terminated!"));
}

public static void processMessage(Message msg, SimpleClientConnection sender) {
    //process according to destination:
    if(!(msg.destination.equals("server"))) {
        //when list of clients complete, find destination.
        /*
        SimpleClientConnection temp;
        for(int i=0; i<SimpleServer.listOfClients.size(); i++) {
            if(SimpleServer.listOfClients.get(i).)
        }
        */
    }
    else {
        switch(msg.messageText) {
            case ":quit":
                System.out.println("Server received quit request from client "+sender.mId+".");
                if(SimpleServer.listOfClients.size()>1) {
                    sendToAll(new Message("Server", msg.sender+" decided to quit the chatroom."));
                }
                sender.running=0;
                break;
            case ":list":
                System.out.println("Server received request to list connected clients from client "+sender.mId+".");
                sendMessageToClient(new Message("Server", "List of Clients:", sender));
                for (SimpleClientConnection client : SimpleServer.listOfClients) {
                    sendMessageToClient(new Message("Server", client.mId + ": <Name unimplemented>", sender));
                }
                break;
            default:
                //Simple broadcast.
                if(listOfClients.size()>1) {
                    for(SimpleClientConnection client: SimpleServer.listOfClients) {
                        if(sender.mId==client.mId)
                            continue;
                        msg.destinationSCC=client;
                        SimpleServer.messageQueue.addMessageToQueue(msg);
                    }
                }
                else {
                    System.out.println("Lonely guy (Name: "+msg.sender+", ID: "+sender.mId+") broadcasting to empty room: "+msg.messageText);
                }
                break;
        }
    }
}
}

class SimpleClientConnection extends Thread {
    long mId;
    Socket mSocket;
    int running;
    
    public SimpleClientConnection(long id, Socket s) throws Exception {
        super();
        mId = id;
        mSocket = s;
    }

    @Override
    public void run() {
        running=1;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            while(running==1) {
                String read=in.readLine();
                Message m=(new Message(read));
                //System.out.println("Got message "+read);
                SimpleServer.processMessage(m, this);
            }
        } catch (IOException e) {
            //e.printStackTrace(System.out);
            System.out.println("Client Disconnected (Error: "+e+").");
        } finally { 
            try {
            mSocket.close();
            } catch (IOException e) { System.out.println("Error closing socket: "+e); }
            System.out.println("Client ID " + mId + " connection listener terminated!");
            SimpleServer.removeClient(this);
        }
    }
}