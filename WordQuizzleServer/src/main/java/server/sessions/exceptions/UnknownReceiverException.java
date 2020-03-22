package server.sessions.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class UnknownReceiverException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.UNKNOWN_RECEIVER_EXCEPTION;
    private static final String DEFAULT_MESSAGE = "RECEIVER UNKNOWN";

    public UnknownReceiverException()
    {
        super(DEFAULT_MESSAGE);
    }

    public UnknownReceiverException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
