package qstp;

import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.Socket;
import java.io.IOException;

/**
 *
 * @author Vinay
 */
class ClientMessageQueue extends Thread {
    
    private LinkedBlockingQueue<Message> messages;
    private Socket outputSocket;
    
    public ClientMessageQueue(Socket sock) {
        messages = new LinkedBlockingQueue<Message>();
        outputSocket = sock;
    }
    @Override
    public void run() {
        System.out.println("Message Queue started.");
        try{
        ObjectOutputStream oos =
                new ObjectOutputStream(outputSocket.getOutputStream());
            while(SimpleClient.listener.running) {
                if(messages.size()>0) {
                    //send message
                    Message m=messages.take();
                    try{
                    oos.writeObject(m);
                    oos.flush();
                    } catch (IOException err) { System.out.println("Message '"+m.messageText+"' could not be sent."); }
                }
            }
        } catch (Exception err) { System.out.println("Error: " + err); }
    }
    public void addMessageToQueue(Message msg) throws InterruptedException {
        messages.put(msg);
    }
}