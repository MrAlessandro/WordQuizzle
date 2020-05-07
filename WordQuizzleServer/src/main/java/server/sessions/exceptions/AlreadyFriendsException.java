package server.sessions.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class AlreadyFriendsException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.ALREADY_FRIENDS;
    private static final String DEFAULT_MESSAGE = "USERS ARE ALREADY FRIENDS";

    public AlreadyFriendsException()
    {
        super(DEFAULT_MESSAGE);
    }

    public AlreadyFriendsException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
