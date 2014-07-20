package qstp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;

/**
 *
 * @author Vinay
 */
public class SimpleServer implements Runnable {

    HashMap<Long, SimpleClientConnection> mClients;
    ServerMessageQueue mMessageQueue;

    public static void main(String args[]) {
        (new Thread(new SimpleServer())).start();
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(8888);
            mClients = new HashMap<Long, SimpleClientConnection>();
            mMessageQueue = new ServerMessageQueue();
            mMessageQueue.start();
            long currentId = 1;
            System.out.println("Server listening for connections!");
            do {
                Socket s = server.accept();
                System.out.println("New connection! ID: " + currentId);
                broadcast(new Message("MESSAGE", -1, 0, "New connection! ID: " + currentId + ". Say Hi!"));
                SimpleClientConnection newclient = new SimpleClientConnection(currentId, s);
                newclient.start();
                mClients.put(currentId, newclient);
                sendMessageToClient(new Message("MESSAGE", -1, currentId, "Welcome to the server. Your ID is: " + (currentId)));
                sendMessageToClient(new Message("IDINFO", -1, currentId, currentId + ""));
                currentId++;
                System.out.println("Number of currently connected clients: " + mClients.size());
            } while (mClients.size() > 0);  //for successfully closing when no clients remain connected, need to run the acceptor in a different thread, perhaps use stop(). (to do)
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void broadcast(Message msg) { //requires only mSenderID and mText.
        for (SimpleClientConnection client : mClients.values()) {
            if (client.getMId() == msg.mSenderID) {
                continue;
            }
            msg.mDestinationID=client.mId;
            mMessageQueue.addMessageToQueue(msg);
        }
    }

    public void sendMessageToClient(Message msg) { //requires mSenderID, mText, and mDestinationID
        mMessageQueue.addMessageToQueue(msg);
    }

    public void clientQuit(long ID) {
        mClients.remove(ID);
        broadcast(new Message("MESSAGE", -1, 0, "Client ID: " + ID + " - connection terminated!"));
    }

    public void processMessage(Message msg, SimpleClientConnection sender) {
        //process according to mDestinationID:
        //-1: Server
        //0: Broadcast.
        //>0: Client-to-client.
        if (msg.mDestinationID > 0) {
            sendMessageToClient(msg);
            //when list of clients complete, find mDestinationID (on client side)
            /*
             SimpleClientConnection temp;
             for(int i=0; i<SimpleServer.mClients.size(); i++) {
             if(SimpleServer.mClients.get(i).)
             }
             */
        } else {
            switch (msg.mText) {
                case ":quit":
                    System.out.println("Server received quit request from client " + sender.mId + ".");
                    if (mClients.size() > 1) {
                        broadcast(new Message("MESSAGE", -1, 0, msg.mSenderID + " decided to quit the chatroom."));
                    }
                    sender.quit();
                    break;
                case ":list":
                    System.out.println("Server received request to list connected clients from client " + sender.mId + ".");
                    sendMessageToClient(new Message("MESSAGE", -1, sender.mId, "List of Clients:"));
                    for (SimpleClientConnection client : mClients.values()) {
                        sendMessageToClient(new Message("MESSAGE", -1, sender.mId, client.mId + ": <Name unimplemented>"));
                    }
                    break;
                default:
                    System.out.println("Unrecognized keyword in message: " + msg.getString());
                    sendMessageToClient(new Message("MESSAGE", -1, sender.mId, "Unrecognized keyword in message: " + msg.mText));
                    broadcast(msg);
                    break;
            }
        }
    }

    class ServerMessageQueue extends Thread {

        private LinkedBlockingQueue<Message> mMessages;

        public ServerMessageQueue() {
            mMessages = new LinkedBlockingQueue<Message>();
        }

        @Override
        public void run() {
            System.out.println("Server Message Queue started.");
            while (true) {
                try {
                    if (mMessages.size() > 0) {
                        System.out.println("Number of pending messages: " + mMessages.size());
                        Message m = mMessages.take();
                        mClients.get(m.mDestinationID).sendMessage(m);
                    }
                } catch (InterruptedException err) {
                    System.out.println("Error accessing message queue: " + err);
                }
            }
        }

        public void addMessageToQueue(Message msg) {
            //System.out.println("Adding message: "+msg.getString()+" to server message queue.");
            try {
                mMessages.put(msg);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e);
            }
        }
    }

    class SimpleClientConnection extends Thread {

        long mId;
        Socket mSocket;

        private final long mPingRate = 10 * 1000; // 2 minutes.
        private final long mPingTimeout = 10 * 1000; // 2 minutes.
        private Timer mPingTimer = new Timer();
                TimerTask mPingTaskRef = mPingTask();
                TimerTask mTimeoutTaskRef = mTimeoutTask();
        private TimerTask mPingTask() {
            return ( new TimerTask() {
                @Override
                public void run() {
                    sendMessage(new Message("PING", -1, mId, "PingFromServer"));
                    System.out.println("Ping sent. Scheduling quit task.");
                    mTimeoutTaskRef = mTimeoutTask();
                    mPingTimer.schedule(mTimeoutTaskRef, mPingTimeout);
                }
            });
        }
        private TimerTask mTimeoutTask() {
            return (new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Quitting client thread due to ping timeout.");
                    quit();
                }
            });
        }
        public SimpleClientConnection(long id, Socket s) {
            super();
            mId = id;
            mSocket = s;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                String read;
                mPingTimer.schedule(mPingTaskRef, mPingRate);
                while ((read = in.readLine()) != null) {
                    Message m = (new Message(read));
                    switch (m.mType) {
                        case "PING":
                            System.out.println("Got ping. Sending pong.");
                            sendPong();
                            break;
                        case "PONG":
                            System.out.println("Got pong. Resetting timers.");
                            mTimeoutTaskRef.cancel();
                            mPingTaskRef = mPingTask();
                            mPingTimer.schedule(mPingTaskRef, mPingRate);
                            break;
                        default:
                            //System.out.println("Got message "+read);
                            processMessage(m, this);
                            break;
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace(System.out);
                System.out.println("Client Disconnected (Error: " + e + ").");
            } finally {
                quit();
                System.out.println("Client ID " + mId + " connection listener terminated!");
            }
        }

        public void sendMessage(Message m) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(mSocket.getOutputStream());
                osw.write(m.getString() + "\n");
                osw.flush();
                System.out.println("Message sent (" + m.getString() + ").");
            } catch (IOException err) {
                System.out.println("Message '" + m.getString() + "' could not be sent. Error: " + err);
            }
        }

        public void sendPong() {
            sendMessage(new Message("PONG", -1, mId, "PONGFROMSERVER"));
        }

        public long getMId() {
            return mId;
        }

        public void quit() {
            try {
                mSocket.close();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
            clientQuit(mId);
        }
    }
}