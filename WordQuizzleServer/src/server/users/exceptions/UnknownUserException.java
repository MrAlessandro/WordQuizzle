package server.users.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class UnknownUserException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.USERNAME_UNKNOWN;
    private static final String DEFAULT_MESSAGE = "UNKNOWN USER";

    public UnknownUserException()
    {
        super(DEFAULT_MESSAGE);
    }

    public UnknownUserException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
