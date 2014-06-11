/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        Socket sock = new Socket("localhost", 8888);
        SimpleClientListener listener = new SimpleClientListener(sock);
        listener.start();
        System.out.println("Now receiving broadcasted messages from server.");
        Scanner s = new Scanner(System.in);
        OutputStreamWriter osw =
                new OutputStreamWriter(
                sock.getOutputStream());
        String line;
        //System.out.print("You: ");
        while (true) {
            line = s.nextLine();
            osw.write(line + "\n");
            osw.flush();
            if(line.equals(":quit"))
                break;
            System.out.print("You: ");
        }
        System.out.println("You have quit the chat program.");
        listener.running=0;
        s.close();
    }
}

class SimpleClientListener extends Thread {
    
    private Socket mSocket;
    int running;
    
    public SimpleClientListener(Socket s) throws Exception {
        super();
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
            while ((line = br.readLine()) != null && (running == 1) ) {
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
