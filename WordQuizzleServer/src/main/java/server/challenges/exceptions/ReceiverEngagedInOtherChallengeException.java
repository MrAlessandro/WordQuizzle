package server.challenges.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class ReceiverEngagedInOtherChallengeException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.RECEIVER_ENGAGED_IN_OTHER_CHALLENGE;
    private static final String DEFAULT_MESSAGE = "RECEIVER ENGAGED IN OTHER CHALLENGE";

    public ReceiverEngagedInOtherChallengeException()
    {
        super(DEFAULT_MESSAGE);
    }

    public ReceiverEngagedInOtherChallengeException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
