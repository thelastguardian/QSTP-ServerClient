package qstp;

/**
 *
 * @author Vinay
 */
public class Message {
    String messageText;
    String sender;
    public Message(String nameOfSender, String messageToSend) {
        messageText=messageToSend;
        sender=nameOfSender;
    }
}