package qstp;

import java.util.Scanner;

/**
 *
 * @author Vinay
 */
public class Message {

    String mType;
    long mSenderID;
    long mDestinationID;
    String mText;

    public Message(String type, long senderID, long destinationID, String text) {
        mType = type;
        mSenderID = senderID;
        mDestinationID = destinationID;
        mText = text;
    }

    public Message(String toParse) {
        Scanner in = new Scanner(toParse);
        in.useDelimiter("~");
        mType = in.next();
        mSenderID = in.nextLong();
        mDestinationID = in.nextLong();
        in.reset();
        mText = in.nextLine().substring(1);
    }

    public String getString() {
        return (mType + "~" + mSenderID + "~" + mDestinationID + "~" + mText);
    }
}
