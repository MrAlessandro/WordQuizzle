package server.requests.challenge.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class ReceiverEngagedInOtherChallengeRequestException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.RECEIVER_ENGAGED_IN_OTHER_CHALLENGE_REQUEST;
    private static final String DEFAULT_MESSAGE = "OPPONENT ENGAGED IN OTHER CHALLENGE REQUEST";

    public ReceiverEngagedInOtherChallengeRequestException()
    {
        super(DEFAULT_MESSAGE);
    }

    public ReceiverEngagedInOtherChallengeRequestException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
