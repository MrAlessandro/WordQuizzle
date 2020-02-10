package server.users.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class OpponentAlreadyEngagedException extends CommunicableException
{
    public static final MessageType RESPONSE_TYPE = MessageType.OPPONENT_ALREADY_ENGAGED;
    public static final String DEFAULT_MESSAGE = "OPPONENT ALREADY ENGAGED IN OTHER CHALLENGE";

    public OpponentAlreadyEngagedException()
    {
        super(DEFAULT_MESSAGE);
    }

    public OpponentAlreadyEngagedException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
