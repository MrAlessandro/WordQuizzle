package messages.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class UnexpectedMessageException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;
    private static final String DEFAULT_MESSAGE = "UNEXPECTED MESSAGE";

    public UnexpectedMessageException ()
    {
        super(DEFAULT_MESSAGE);
    }

    public UnexpectedMessageException (String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType() {
        return RESPONSE_TYPE;
    }
}
