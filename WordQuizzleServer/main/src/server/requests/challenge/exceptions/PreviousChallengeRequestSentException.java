package server.requests.challenge.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class PreviousChallengeRequestSentException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.PREVIOUS_CHALLENGE_REQUEST_SENT;
    private static final String DEFAULT_MESSAGE = "PREVIOUS CHALLENGE REQUEST SENT";

    public PreviousChallengeRequestSentException()
    {
        super(DEFAULT_MESSAGE);
    }

    public PreviousChallengeRequestSentException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
