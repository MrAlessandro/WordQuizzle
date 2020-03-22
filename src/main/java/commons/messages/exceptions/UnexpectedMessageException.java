package commons.messages.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.Message;
import commons.messages.MessageType;

/**
 * Signals the reading of an unexpected message. It can be thrown when has been received a message {@link Message}
 * in an unexpected sequence. For example, if a connection did not established a {@code Session} and send a friendship
 * request to a user
 * @author Alessandro Meschi
 * @version 1.0
 */
public class UnexpectedMessageException extends CommunicableException
{
    /**
     * The response {@link MessageType} relative to this exception
     */
    private static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;

    /**
     * The default message relative to this exception
     */
    private static final String DEFAULT_MESSAGE = "UNEXPECTED MESSAGE";

    /**
     * Initialize a new instance of {@link UnexpectedMessageException} passing the {@code DEFAULT_MESSAGE} to the
     * {@link CommunicableException} constructor
     */
    public UnexpectedMessageException()
    {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Initialize a new {@link UnexpectedMessageException}
     * @param message Message to be passed to the {@link CommunicableException} constructor
     */
    public UnexpectedMessageException(String message) {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
