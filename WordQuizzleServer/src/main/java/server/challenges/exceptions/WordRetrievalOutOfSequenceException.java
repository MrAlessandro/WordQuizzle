package server.challenges.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class WordRetrievalOutOfSequenceException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;
    private static final String DEFAULT_MESSAGE = "WORD RETRIEVAL OUT OF SEQUENCE";

    public WordRetrievalOutOfSequenceException()
    {
        super(DEFAULT_MESSAGE);
    }

    public WordRetrievalOutOfSequenceException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}