/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package qstp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author nhnt11
 */
public class SimpleServer {
    public static void main(String args[]) throws Exception {
        ServerSocket server = new ServerSocket(8888);
        long currentId = 0;
        System.out.println("Server listening for connections!");
        while (true) {
            Socket s = server.accept();
            System.out.println("New connection! ID: " + currentId);
            new SimpleClientConnection(currentId++, s).start();
        }
    }
}

class SimpleClientConnection extends Thread {
    private long mId;
    private Socket mSocket;
    
    public SimpleClientConnection(long id, Socket s) throws Exception {
        super();
        mId = id;
        mSocket = s;
    }

    @Override
    public void run() {
        try {
            BufferedReader br =
                    new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(mId + ": " + line);
            }
            mSocket.close();
            System.out.println("Client " + mId + " terminated!");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}