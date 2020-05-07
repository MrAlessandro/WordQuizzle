package server.users.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class UnknownUserException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.USERNAME_UNKNOWN;
    private static final String DEFAULT_MESSAGE = "UNKNOWN USER";
    private String username;

    public UnknownUserException(String username)
    {
        super(DEFAULT_MESSAGE);
        this.username = username;
    }

    public UnknownUserException(String username, String message)
    {
        super(message);
        this.username = username;
    }

    public String getUsername()
    {
        return this.username;
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
