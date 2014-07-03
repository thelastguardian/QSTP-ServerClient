package qstp;

import java.io.OutputStreamWriter;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.Socket;
import java.io.IOException;

/**
 *
 * @author Vinay
 */
class MessageQueue extends Thread {
    
    private LinkedBlockingQueue<Message> messages;
    private Socket outputSocket;
    
    public MessageQueue(Socket sock) {
        messages = new LinkedBlockingQueue<Message>();
        outputSocket = sock;
    }
    @Override
    public void run() {
        System.out.println("Message Queue started.");
        try{
        OutputStreamWriter osw =
                new OutputStreamWriter(
                outputSocket.getOutputStream());
            while(SimpleClient.listener.running) {
                if(messages.size()>0) {
                    //send message
                    Message m=messages.take();
                    try{
                    osw.write(m.messageText + "\n");
                    osw.flush();
                    } catch (IOException err) { System.out.println("Message '"+m.messageText+"' could not be sent. Will try again soon."); }
                }
            }
        } catch (Exception err) { System.out.println("Error: " + err); }
    }
    public void addMessageToQueue(Message msg) throws InterruptedException {
        messages.put(msg);
    }
}