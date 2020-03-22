package server.challenges.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class NoFurtherWordsToGetException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;
    private static final String DEFAULT_MESSAGE = "NO FURTHER WORDS TO GET BY USER FOR THIS CHALLENGE";

    public NoFurtherWordsToGetException()
    {
        super(DEFAULT_MESSAGE);
    }

    public NoFurtherWordsToGetException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
