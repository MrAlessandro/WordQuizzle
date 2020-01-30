package server.users.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class WrongPasswordException extends CommunicableException
{
    public static final MessageType RESPONSE_TYPE = MessageType.PASSWORD_WRONG;
    public static final String DEFAULT_MESSAGE = "PASSWORD WRONG";

    public WrongPasswordException(String message)
    {
        super(message);
    }

    public WrongPasswordException()
    {
        super(DEFAULT_MESSAGE);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
