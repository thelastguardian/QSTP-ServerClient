package qstp;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author nhnt11
 */
public class SimpleClient {
    static String name;
    static SimpleClientListener listener;
    
    public static void main(String[] args) throws Exception {
        Socket sock = new Socket("localhost", 8888);
        Scanner s = new Scanner(System.in);
        System.out.print("Please enter your name: ");
        name=s.nextLine();
        System.out.println("Welcome, " + name + ". You may now send messages.");
        listener = new SimpleClientListener(sock);
        listener.start();
        String line;
        OutputStreamWriter oos =
                new OutputStreamWriter(sock.getOutputStream());
        //System.out.print("You: ");
        while (listener.running && (line = s.nextLine())!=null) {
            Message toSend = new Message(name, line, "server");
            try {
                oos.write(toSend.getString()+"\n");
                oos.flush();
            } catch (IOException err) { System.out.println("Message '"+toSend.getString()+"' could not be sent. (Error: "+err+")"); }
            if(line.equals(":quit"))
                break;
            System.out.print("You: ");
        }
        s.close();
        System.out.println("You have quit the chat program.");
        listener.running=false;
    }
}

class SimpleClientListener extends Thread {
    
    private Socket mSocket;
    boolean running;
    
    public SimpleClientListener(Socket s) throws Exception {
        super();
        mSocket = s;
    }

    @Override
    public void run() {
        running=true;
        System.out.print("Connecting to server... ");
        BufferedReader in;
        try {
            in = new BufferedReader(
                new InputStreamReader(mSocket.getInputStream()));
            System.out.println("connected successfully. Now listening for messages.");
            String read;
            while ((read = in.readLine())!=null && running) {
                System.out.print("\b\b\b\b\b\b");
                Message received = new Message(read);
                System.out.println(received.sender+": "+received.messageText);
                System.out.print("You: ");
            }
        } catch (IOException e) { 
            //e.printStackTrace(System.out);
            System.out.println("Server connection lost.");
        } finally {
            try {
            mSocket.close();
            } catch (IOException ioe) { System.out.println("Error closing socket: "+ioe); }
            System.out.println("Listener stopped. No longer receiving messages from server.");
        }
    }
}