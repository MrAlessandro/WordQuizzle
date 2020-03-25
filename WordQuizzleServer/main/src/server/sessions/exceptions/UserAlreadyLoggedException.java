package server.sessions.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class UserAlreadyLoggedException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.USER_ALREADY_LOGGED;
    private static final String DEFAULT_MESSAGE = "USER ALREADY LOGGED";

    public UserAlreadyLoggedException()
    {
        super(DEFAULT_MESSAGE);
    }

    public UserAlreadyLoggedException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
