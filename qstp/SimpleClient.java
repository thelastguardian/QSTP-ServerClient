package qstp;

import java.io.BufferedReader;
import java.io.IOException;
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
        // TODO code application logic here
        Socket sock = new Socket("localhost", 8888);
        listener = new SimpleClientListener(sock);
        listener.start();
        System.out.println("Now receiving broadcasted messages from server.");
        MessageQueue mq = new MessageQueue(sock);
        Scanner s = new Scanner(System.in);
        System.out.print("Please enter your name: ");
        name=s.nextLine();
        System.out.println("Welcome, " + name + ". You may now send messages.");
        String line;
        //System.out.print("You: ");
        while (true) {
            line = s.nextLine();
            mq.addMessageToQueue(new Message(name, line));
            if(line.equals(":quit"))
                break;
            System.out.print("You: ");
        }
        System.out.println("You have quit the chat program.");
        listener.running=false;
        s.close();
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
        try {
            BufferedReader br =
                    new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream()));
            String line;
            while ((line = br.readLine()) != null && (running) ) {
                System.out.print("\b\b\b\b\b     \b\b\b\b\b");
                System.out.println(line);
                System.out.print("You: ");
            }
            System.out.println("Listener stopped");
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}