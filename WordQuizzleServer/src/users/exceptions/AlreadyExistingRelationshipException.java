package users.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class AlreadyExistingRelationshipException extends CommunicableException
{
    public static final MessageType RESPONSE_TYPE = MessageType.ALREADY_FRIENDS;
    public static final String DEFAULT_MESSAGE = "RELATIONSHIP ALREADY EXISTS";

    public AlreadyExistingRelationshipException()
    {
        super(DEFAULT_MESSAGE);
    }

    public AlreadyExistingRelationshipException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
