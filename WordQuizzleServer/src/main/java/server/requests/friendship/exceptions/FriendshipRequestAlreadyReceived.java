package server.requests.friendship.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class FriendshipRequestAlreadyReceived extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.FRIENDSHIP_REQUEST_ALREADY_RECEIVED;
    private static final String DEFAULT_MESSAGE = "FRIENDSHIP REQUEST ALREADY RECEIVED";

    public FriendshipRequestAlreadyReceived()
    {
        super(DEFAULT_MESSAGE);
    }

    public FriendshipRequestAlreadyReceived(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
