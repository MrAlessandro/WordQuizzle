package server.challenges.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class TranslationProvisionOutOfSequenceException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;
    private static final String DEFAULT_MESSAGE = "TRANSLATION PROVISION OUT OF SEQUENCE";

    public TranslationProvisionOutOfSequenceException()
    {
        super(DEFAULT_MESSAGE);
    }

    public TranslationProvisionOutOfSequenceException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
