package qstp;

import java.util.Scanner;

/**
 *
 * @author Vinay
 */
public class Message {

    String sender;
    String messageText;
    String destination;
    SimpleClientConnection destinationSCC;

    public Message(String nameOfSender, String messageToSend, String dest) {
        messageText = messageToSend;
        sender = nameOfSender;
        destination = dest;
    }

    public Message(String nameOfSender, String messageToSend, SimpleClientConnection destSCC) {
        messageText = messageToSend;
        sender = nameOfSender;
        destinationSCC = destSCC;
        destination = "mID: " + destSCC.mId;
    }

    public Message(String nameOfSender, String messageToSend) {
        messageText = messageToSend;
        sender = nameOfSender;
    }

    public Message(String toParse) {
        Scanner in = new Scanner(toParse);
        in.useDelimiter("~");
        sender = in.next();
        messageText = in.next();
        destination = in.next();
    }

    public String getString() {
        return (sender + "~" + messageText + "~" + destination);
    }
}
