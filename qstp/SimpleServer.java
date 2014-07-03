package qstp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 *
 * @author nhnt11
 */
public class SimpleServer {
    static Vector listOfClients;
    public static void main(String args[]) throws Exception {
        ServerSocket server = new ServerSocket(8888);
        listOfClients = new Vector();
        long currentId = 0;
        System.out.println("Server listening for connections!");
        while (true) {
            Socket s = server.accept();
            System.out.println("New connection! ID: " + currentId);
            sendToAll("New connection! ID: " + currentId + ". Say Hi!", -1);
            SimpleClientConnection newclient = new SimpleClientConnection(currentId++, s);
            newclient.start();            
            listOfClients.add(newclient);
            sendMessageToClient(newclient, "Welcome to the server. Your ID is: " + (currentId-1));
        }
    }
public static void removeClientId(long Id) {
    listOfClients.removeElement(Id);
}
public static void sendToAll(String message, long fromId) throws Exception {
    for(int i=0; i<listOfClients.size(); i++) {
        if(fromId==((SimpleClientConnection)(listOfClients.elementAt(i))).mId)
            continue;
        sendMessageToClient(((SimpleClientConnection)(listOfClients.elementAt(i))), (fromId==-1 ? "Server" : fromId) + ": " + message);
    }
}
public static void sendMessageToClient(SimpleClientConnection cc, String message) throws Exception {
    OutputStreamWriter osw =
                new OutputStreamWriter(cc.mSocket.getOutputStream());
        osw.write(message+"\n");
        osw.flush();
}
public static void removeClient(SimpleClientConnection cc) throws Exception {
    
            SimpleServer.sendToAll("Client " + cc.mId + " terminated! \n", cc.mId);
            SimpleServer.listOfClients.removeElement(cc);
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
            BufferedReader br =
                    new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream()));
            String line;
            while(running==1) {
                line = br.readLine();
                switch (line) {
                    case ":quit":
                        System.out.println("Server received quit request from client.");
                        running=0;
                        break;
                    case ":list":
                        System.out.println("Server received request to list connected clients.");
                        System.out.print("Clients: ");
                        for(int i=0; i<SimpleServer.listOfClients.size(); i++) {
                            SimpleServer.sendMessageToClient(this, (((SimpleClientConnection)(SimpleServer.listOfClients.elementAt(i))).mId)+"");
                            System.out.print((((SimpleClientConnection)(SimpleServer.listOfClients.elementAt(i))).mId)+",");
                        }   
                        System.out.println();
                        break;
                    default:
                        System.out.println(mId + ": " + line);
                        break;
                }
                    SimpleServer.sendToAll(line, mId);
            }
            mSocket.close();
            System.out.println("Client " + mId + " terminated!");
            SimpleServer.removeClient(this);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}