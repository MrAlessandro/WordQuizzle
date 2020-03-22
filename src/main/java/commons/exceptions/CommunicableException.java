package commons.exceptions;

import commons.messages.MessageType;

/**
 * Abstract class that is the base for exceptions which have a message type related to them.
 * This makes easy the process of exceptional situation detection and further communication
 * of it to the other counterpart (server to client and vice versa).
 * @author Alessandro Meschi
 * @version 1.0
 */
public abstract class CommunicableException extends Exception
{
    /**
     * Initialize a new {@link CommunicableException}
     */
    public CommunicableException(String message)
    {
        super(message);
    }

    /**
     * Gets the {@link MessageType} relative to this exception
     * @return The {@link MessageType} relative to this exception
     */
    public abstract MessageType getResponseType();
}
