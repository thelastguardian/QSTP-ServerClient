package qstp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public static void main(String args[]) {
        try {
            ServerSocket server = new ServerSocket(8888);
            listOfClients = new ArrayList<SimpleClientConnection>();
            messageQueue = new ServerMessageQueue();
            messageQueue.start();
            long currentId = 0;
            System.out.println("Server listening for connections!");
            do {
                Socket s = server.accept();
                System.out.println("New connection! ID: " + currentId);
                sendToAll(new Message("MESSAGE", "Server", "New connection! ID: " + currentId + ". Say Hi!"));
                SimpleClientConnection newclient = new SimpleClientConnection(currentId++, s);
                newclient.start();
                listOfClients.add(newclient);
                sendMessageToClient(new Message("MESSAGE", "Server", "Welcome to the server. Your ID is: " + (currentId - 1), newclient));
                System.out.println("Number of currently connected clients: " + listOfClients.size());
            } while (listOfClients.size() > 0);  //for successfully closing when no clients remain connected, need to run the acceptor in a different thread, perhaps use stop(). (to do)
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void sendToAll(Message msg) { //requires only mSender and mText.
        for (SimpleClientConnection client : SimpleServer.listOfClients) {
            msg.mDestinationSCC = client;
            SimpleServer.messageQueue.addMessageToQueue(msg);
        }
    }

    public static void sendMessageToClient(Message msg) { //requires mSender, mText, and destSCC
        SimpleServer.messageQueue.addMessageToQueue(msg);
    }

    public static void removeClient(SimpleClientConnection cc) {
        SimpleServer.listOfClients.remove(cc);
        SimpleServer.sendToAll(new Message("MESSAGE", "Server", "Client ID: " + cc.mId + " - connection terminated!"));
    }

    public static void processMessage(Message msg, SimpleClientConnection sender) {
        //process according to mDestination:
        if (!(msg.mDestination.equals("server"))) {
            //when list of clients complete, find mDestination.
            /*
             SimpleClientConnection temp;
             for(int i=0; i<SimpleServer.listOfClients.size(); i++) {
             if(SimpleServer.listOfClients.get(i).)
             }
             */
        } else {
            switch (msg.mText) {
                case ":quit":
                    System.out.println("Server received quit request from client " + sender.mId + ".");
                    if (SimpleServer.listOfClients.size() > 1) {
                        sendToAll(new Message("MESSAGE", "Server", msg.mSender + " decided to quit the chatroom."));
                    }
                    sender.close();
                    break;
                case ":list":
                    System.out.println("Server received request to list connected clients from client " + sender.mId + ".");
                    sendMessageToClient(new Message("MESSAGE", "Server", "List of Clients:", sender));
                    for (SimpleClientConnection client : SimpleServer.listOfClients) {
                        sendMessageToClient(new Message("MESSAGE", "Server", client.mId + ": <Name unimplemented>", sender));
                    }
                    break;
                default:
                    //Simple broadcast.
                    if (listOfClients.size() > 1) {
                        for (SimpleClientConnection client : SimpleServer.listOfClients) {
                            if (sender.mId == client.mId) {
                                continue;
                            }
                            msg.mDestinationSCC = client;
                            SimpleServer.messageQueue.addMessageToQueue(msg);
                        }
                    } else {
                        System.out.println("Lonely guy (Name: " + msg.mSender + ", ID: " + sender.mId + ") broadcasting to empty room: " + msg.mText);
                    }
                    break;
            }
        }
    }
}

class SimpleClientConnection extends Thread {

    long mId;
    Socket mSocket;

    public SimpleClientConnection(long id, Socket s) throws Exception {
        super();
        mId = id;
        mSocket = s;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            String read;
            while ((read = in.readLine()) != null) {
                Message m = (new Message(read));
                //System.out.println("Got message "+read);
                SimpleServer.processMessage(m, this);
            }
        } catch (IOException e) {
            //e.printStackTrace(System.out);
            System.out.println("Client Disconnected (Error: " + e + ").");
        } finally {
            try {
                mSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e);
            }
            System.out.println("Client ID " + mId + " connection listener terminated!");
            SimpleServer.removeClient(this);
        }
    }

    public void close() {
        try {
            mSocket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}
