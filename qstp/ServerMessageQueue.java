package qstp;

import java.io.ObjectOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.IOException;

/**
 *
 * @author Vinay
 */
class ServerMessageQueue extends Thread {
    
    private LinkedBlockingQueue<Message> messages;
    
    public ServerMessageQueue() {
        messages = new LinkedBlockingQueue<Message>();
    }
    @Override
    public void run() {
        System.out.println("Server Message Queue started.");
        try{
            /*
        OutputStreamWriter osw =
                new OutputStreamWriter(
                outputSocket.getOutputStream());*/
            while(SimpleClient.listener.running) {
                if(messages.size()>0) {
                    //send message
                    Message m=messages.take();
                    try{
                        ObjectOutputStream oos = new ObjectOutputStream(m.destinationSCC.mSocket.getOutputStream());
                        oos.writeObject(m);
                        oos.flush();
                    } catch (IOException err) { System.out.println("Message '"+m.messageText+"' could not be sent."); }
                }
            }
        } catch (Exception err) { System.out.println("Error: " + err); }
    }
    public void addMessageToQueue(Message msg) {
        try{
            messages.put(msg);
        }catch (InterruptedException e) { System.out.println("Error: "+e); }
    }
}