package server.requests.friendship.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class FriendshipRequestAlreadySent extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.FRIENDSHIP_REQUEST_ALREADY_SENT;
    private static final String DEFAULT_MESSAGE = "FRIENDSHIP REQUEST ALREADY SENT";

    public FriendshipRequestAlreadySent()
    {
        super(DEFAULT_MESSAGE);
    }

    public FriendshipRequestAlreadySent(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
