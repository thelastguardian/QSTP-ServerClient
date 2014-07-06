package qstp;

import java.util.Scanner;

/**
 *
 * @author Vinay
 */
public class Message {

    String mType;
    String mSender;
    String mDestination;
    String mText;
    SimpleClientConnection mDestinationSCC;

    public Message(String type, String sender, String destination, String text) {
        mType = type;
        mSender = sender;
        mDestination = destination;
        mText = text;
    }

    public Message(String type, String sender, String text, SimpleClientConnection destinationSCC) {
        mType = type;
        mSender = sender;
        mText = text;
        mDestinationSCC = destinationSCC;
        mDestination = "mID: " + destinationSCC.mId;
    }

    public Message(String type, String sender, String text) {
        mType = type;
        mSender = sender;
        mText = text;
    }

    public Message(String toParse) {
        Scanner in = new Scanner(toParse);
        in.useDelimiter("~");
        mType = in.next();
        mSender = in.next();
        mDestination = in.next();
        in.reset();
        mText = in.nextLine().substring(1);
    }

    public String getString() {
        return (mType + "~" + mSender + "~" + mDestination + "~" + mText);
    }
}
