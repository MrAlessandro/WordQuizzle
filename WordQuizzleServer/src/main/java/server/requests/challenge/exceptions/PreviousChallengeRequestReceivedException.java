package server.requests.challenge.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class PreviousChallengeRequestReceivedException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.PREVIOUS_CHALLENGE_REQUEST_RECEIVED;
    private static final String DEFAULT_MESSAGE = "PREVIOUS CHALLENGE REQUEST RECEIVED";

    public PreviousChallengeRequestReceivedException()
    {
        super(DEFAULT_MESSAGE);
    }

    public PreviousChallengeRequestReceivedException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
