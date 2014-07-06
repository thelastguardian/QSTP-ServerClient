package qstp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.LinkedBlockingQueue;

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
        try {
            while (SimpleServer.running) {
                if (messages.size() > 0) {
                    //send message
                    System.out.println("Number of pending messages: " + messages.size());
                    Message m = messages.take();
                    try {
                        OutputStreamWriter osw = new OutputStreamWriter(m.destinationSCC.mSocket.getOutputStream());
                        osw.write(m.getString() + "\n");
                        osw.flush();
                        System.out.println("Message sent (" + m.getString() + ").");
                    } catch (IOException err) {
                        System.out.println("Message '" + m.getString() + "' could not be sent. Error: " + err);
                    }
                }
            }
        } catch (InterruptedException err) {
            System.out.println("Error accessing message queue: " + err);
        }
    }

    public void addMessageToQueue(Message msg) {
        //System.out.println("Adding message: "+msg.getString()+" to server message queue.");
        try {
            messages.put(msg);
        } catch (InterruptedException e) {
            System.out.println("Error: " + e);
        }
    }
}
