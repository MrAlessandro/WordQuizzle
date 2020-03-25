package server.challenges.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class NoChallengeRelatedException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;
    private static final String DEFAULT_MESSAGE = "NO CHALLENGE RELATED FOR THIS USER";

    public NoChallengeRelatedException()
    {
        super(DEFAULT_MESSAGE);
    }

    public NoChallengeRelatedException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
