package qstp;

import java.io.Serializable;

/**
 *
 * @author Vinay
 */
public class Message implements Serializable {
    String messageText;
    String sender;
    String destination;
    SimpleClientConnection destinationSCC;
    public Message(String nameOfSender, String messageToSend, String dest) {
        messageText=messageToSend;
        sender=nameOfSender;
        destination=dest;
    }
    public Message(String nameOfSender, String messageToSend, SimpleClientConnection destSCC) {
        messageText=messageToSend;
        sender=nameOfSender;
        destinationSCC=destSCC;
    }
    public Message(String nameOfSender, String messageToSend) {
        messageText=messageToSend;
        sender=nameOfSender;
    }
}