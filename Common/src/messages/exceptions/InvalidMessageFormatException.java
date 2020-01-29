package messages.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class InvalidMessageFormatException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.INVALID_MESSAGE_FORMAT;
    private static final String DEFAULT_MESSAGE = "INVALID MESSAGE FORMAT";

    public InvalidMessageFormatException()
    {
        super(DEFAULT_MESSAGE);
    }

    public InvalidMessageFormatException(String message) {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
