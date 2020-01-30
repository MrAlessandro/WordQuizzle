package server.users.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class RequestAlreadySentException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.FRIENDSHIP_REQUEST_ALREADY_SENT;
    private static final String DEFAULT_MESSAGE = "FRIENDSHIP REQUEST ALREADY SENT";

    public RequestAlreadySentException()
    {
        super(DEFAULT_MESSAGE);
    }

    public RequestAlreadySentException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return null;
    }
}
