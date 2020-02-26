package server.users.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class OpponentNotFriendException extends CommunicableException
{
    public static final MessageType RESPONSE_TYPE = MessageType.OPPONENT_NOT_FRIEND;
    public static final String DEFAULT_MESSAGE = "OPPONENT AND APPLICANT ARE NOT FRIENDS";

    public OpponentNotFriendException()
    {
        super(DEFAULT_MESSAGE);
    }

    public OpponentNotFriendException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
