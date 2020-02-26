package server.challenges.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class InexistentChallengeException extends CommunicableException
{
    public static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;
    public static final String DEFAULT_MESSAGE = "CHALLENGE PROGRESS MESSAGE DO NOT CORRESPOND TO ANY ACTIVE CHALLENGE";

    public InexistentChallengeException()
    {
        super(DEFAULT_MESSAGE);
    }

    public InexistentChallengeException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
