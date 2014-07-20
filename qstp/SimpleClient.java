package qstp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nhnt11
 */
public class SimpleClient {

    String mName;
    Socket mSocket;
    long mId;
    boolean mIDreceived;
    private final long mPingRate = 10 * 1000; // 2 minutes.
    private final long mPingTimeout = 10 * 1000; // 2 minutes.
    private Timer mPingTimer;
                TimerTask mPingTaskRef = mPingTask();
                TimerTask mTimeoutTaskRef = mTimeoutTask();
        private TimerTask mPingTask() {
            return ( new TimerTask() {
                @Override
                public void run() {
                    sendMessage(new Message("PING", -1, mId, "PingFromClient"));
                    System.out.println("Ping sent. Scheduling quit task.");
                    mTimeoutTaskRef = mTimeoutTask();
                    mPingTimer.schedule(mTimeoutTaskRef, mPingTimeout);
                }
            });
        }
    private TimerTask mTimeoutTask() {
        return ( new TimerTask() {
        @Override
        public void run() {
            System.out.println("Quitting server due to ping timeout.");
            quit();
        }
    });}

    public static void main(String[] args) {
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
            System.out.println("Your ID is: " + mId);
            String line;
            OutputStreamWriter oos
                    = new OutputStreamWriter(mSocket.getOutputStream());
            //System.out.print("You: ");
            while ((line = s.nextLine()) != null) {
                Message toSend = new Message("MESSAGE", mId, -1, line);
                sendMessage(toSend);
                if (line.equals(":quit")) {
                    break;
                }
                System.out.print("You: ");
            }
            System.out.println("You have quit the chat program.");
            quit();
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

    public void quit() {
        try {
            mSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e);
        }

    }

    class SimpleClientListener implements Runnable {

        private Socket mSocket;

        public SimpleClientListener(Socket s) {
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
                mPingTimer = new Timer();
                mPingTimer.schedule(mPingTaskRef, mPingRate);
                String read;
                while ((read = in.readLine()) != null) {
                    Message rec = new Message(read);
                    switch (rec.mType) {
                        case "PING":
                            System.out.println("Got ping. Sending pong.");
                            sendPong();
                            continue;
                        case "PONG":
                            System.out.println("Got pong. Resetting timers.");
                            mTimeoutTaskRef.cancel();
                            mPingTaskRef = mPingTask();
                            mPingTimer.schedule(mPingTaskRef, mPingRate);
                            continue;
                        case "IDINFO":
                            mId = Long.parseLong(rec.mText);
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
                quit();
                System.out.println("Listener stopped. No longer receiving messages from server.");
            }
        }

        public void sendPong() {
            sendMessage(new Message("PONG", mId, -1, "PongFromClient"));
        }
    }
}
