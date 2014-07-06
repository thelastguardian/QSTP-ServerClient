package qstp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Vinay
 */
class ServerMessageQueue extends Thread {

    private LinkedBlockingQueue<Message> mMessages;

    public ServerMessageQueue() {
        mMessages = new LinkedBlockingQueue<Message>();
    }

    @Override
    public void run() {
        System.out.println("Server Message Queue started.");
        while (true) {
            try {
                if (mMessages.size() > 0) {
                    //send message
                    System.out.println("Number of pending messages: " + mMessages.size());
                    Message m = mMessages.take();
                    try {
                        OutputStreamWriter osw = new OutputStreamWriter(m.mDestinationSCC.mSocket.getOutputStream());
                        osw.write(m.getString() + "\n");
                        osw.flush();
                        System.out.println("Message sent (" + m.getString() + ").");
                    } catch (IOException err) {
                        System.out.println("Message '" + m.getString() + "' could not be sent. Error: " + err);
                    }
                }
            } catch (InterruptedException err) {
                System.out.println("Error accessing message queue: " + err);
            }
        }
    }

    public void addMessageToQueue(Message msg) {
        //System.out.println("Adding message: "+msg.getString()+" to server message queue.");
        try {
            mMessages.put(msg);
        } catch (InterruptedException e) {
            System.out.println("Error: " + e);
        }
    }
}
