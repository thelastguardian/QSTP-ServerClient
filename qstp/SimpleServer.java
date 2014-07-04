package qstp;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.io.Serializable;

/**
 *
 * @author nhnt11
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
        while (true) {
            Socket s = server.accept();
            System.out.println("New connection! ID: " + currentId);
            sendToAll(new Message("Server", "New connection! ID: " + currentId + ". Say Hi!"));
            SimpleClientConnection newclient = new SimpleClientConnection(currentId++, s);
            newclient.start();            
            listOfClients.add(newclient);
            sendMessageToClient(new Message("Server", "Welcome to the server. Your ID is: " + (currentId-1), newclient));
        }
        }catch (Exception e) { e.printStackTrace(); }
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
    SimpleServer.sendToAll(new Message("Server", "Client " + cc.mId + " terminated! \n"));
    SimpleServer.listOfClients.remove(cc);
}

public static void processMessage(Message msg, SimpleClientConnection sender) {
    //find destination:
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
                System.out.println("Server received quit request from client.");
                sender.running=0;
                break;
            case ":list":
                System.out.println("Server received request to list connected clients.");
                System.out.print("Clients: ");
                for (SimpleClientConnection client : SimpleServer.listOfClients) {
                    sendMessageToClient(new Message("Server", client.mId + ": <Name unimplemented>", sender));
                    //messageQueue.addMessageToQueue(new Message("Server", client.mId + ": <Name unimplemented>", sender));
                }
                break;
            default:
                //Simple broadcast.
                for(SimpleClientConnection client: SimpleServer.listOfClients) {
                    if(sender.mId==client.mId)
                        continue;
                msg.destinationSCC=client;
                SimpleServer.messageQueue.addMessageToQueue(msg);
                }
                break;
        }
    }
}
}

class SimpleClientConnection extends Thread implements Serializable {
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
            ObjectInputStream ois = new ObjectInputStream(mSocket.getInputStream());
            while(running==1) {
                Message m=(Message)(ois.readObject());
                SimpleServer.processMessage(m, this);
            }
            mSocket.close();
            System.out.println("Client " + mId + " terminated!");
            SimpleServer.removeClient(this);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}