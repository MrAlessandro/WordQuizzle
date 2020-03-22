package server.sessions.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class ReceiverOfflineException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.RECEIVER_OFFLINE;
    private static final String DEFAULT_MESSAGE = "RECEIVER OFFLINE";

    public ReceiverOfflineException()
    {
        super(DEFAULT_MESSAGE);
    }

    public ReceiverOfflineException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
