package commons.messages.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;
import commons.messages.Field;

/**
 * Signals the reading of a malformed message. It can happens because the {@link MessageType} code is invalid or
 * because of the amount of fields to be read is a negative number or because has been read a negative size for one of
 * the {@link Field}.
 * @author Alessandro Meschi
 * @version 1.0
 */
public class InvalidMessageFormatException extends CommunicableException
{
    /**
     * The response {@link MessageType} relative to this exception
     */
    private static final MessageType RESPONSE_TYPE = MessageType.INVALID_MESSAGE_FORMAT;

    /**
     * The default message relative to this exception
     */
    private static final String DEFAULT_MESSAGE = "INVALID MESSAGE FORMAT";

    /**
     * Initialize a new instance of {@link InvalidMessageFormatException} passing the {@code DEFAULT_MESSAGE} to the
     * {@link CommunicableException} constructor
     */
    public InvalidMessageFormatException()
    {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Initialize a new {@link InvalidMessageFormatException}
     * @param message Message to be passed to the {@link CommunicableException} constructor
     */
    public InvalidMessageFormatException(String message) {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
