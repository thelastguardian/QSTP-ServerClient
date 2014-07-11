package qstp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.UnknownHostException;

/**
 *
 * @author nhnt11
 */
public class SimpleClient {

    String mName;
    Socket mSocket;
    long mID;
    boolean mIDreceived;

    public static void main(String[] args) throws Exception {
        SimpleClient client = new SimpleClient();
        client.start();
    }

    public void start() {
        try {
            mSocket = new Socket("localhost", 8888);
            Scanner s = new Scanner(System.in);
            System.out.print("Please enter your name (cannot contain '~'): ");
            mName = s.nextLine();
            System.out.println("Welcome, " + mName + ". You may now send messages.");
            Thread listener = new Thread(new SimpleClientListener(mSocket));
            listener.start();
            while (!(mIDreceived)) {
                System.out.println("Waiting for ID from sever.");
                Thread.sleep(1000);
            }
            System.out.println("Your ID is: " + mID);
            String line;
            OutputStreamWriter oos
                    = new OutputStreamWriter(mSocket.getOutputStream());
            //System.out.print("You: ");
            while ((line = s.nextLine()) != null) {
                Message toSend = new Message("MESSAGE", mID, -1, line);
                sendMessage(toSend);
                if (line.equals(":quit")) {
                    break;
                }
                System.out.print("You: ");
            }
            s.close();
            System.out.println("You have quit the chat program.");
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void sendMessage(Message msg) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(mSocket.getOutputStream());
            osw.write(msg.getString() + "\n");
            osw.flush();
        } catch (IOException e) {
            System.out.println("Message '" + msg.getString() + "' could not be sent. (Error: " + e + ")");
        }
    }

    class SimpleClientListener implements Runnable {

        private Socket mSocket;

        public SimpleClientListener(Socket s) throws Exception {
            super();
            mSocket = s;
        }

        @Override
        public void run() {
            System.out.print("Connecting to server... ");
            BufferedReader in;
            try {
                in = new BufferedReader(
                        new InputStreamReader(mSocket.getInputStream()));
                System.out.println("connected successfully. Now listening for messages.");
                String read;
                while ((read = in.readLine()) != null) {
                    Message rec = new Message(read);
                    switch (rec.mType) {
                        case "PING":
                            sendPong(mSocket);
                            continue;
                        case "IDINFO":
                            mID = Long.parseLong(rec.mText);
                            mIDreceived = true;
                            continue;
                    }
                    System.out.print("\b\b\b\b\b\b");
                    Message received = new Message(read);
                    System.out.println(received.mSenderID + ": " + received.mText);
                    System.out.print("You: ");
                }
            } catch (IOException e) {
                //e.printStackTrace(System.out);
                System.out.println("Server connection lost.");
            } finally {
                try {
                    mSocket.close();
                } catch (IOException ioe) {
                    System.out.println("Error closing socket: " + ioe);
                }
                System.out.println("Listener stopped. No longer receiving messages from server.");
            }
        }

        public void sendPong(Socket socket) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                osw.write((new Message("PONG", mID, -1, "PongFromClient")).getString());
                osw.flush();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
        }
    }
}