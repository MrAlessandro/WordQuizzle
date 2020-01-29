package exceptions;

import messages.MessageType;

public abstract class CommunicableException extends Exception
{
    public CommunicableException(String message)
    {
        super(message);
    }

    public abstract MessageType getResponseType();
}
