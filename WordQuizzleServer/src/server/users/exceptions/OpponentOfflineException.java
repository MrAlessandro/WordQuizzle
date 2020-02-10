package server.users.exceptions;

import exceptions.CommunicableException;
import messages.MessageType;

public class OpponentOfflineException extends CommunicableException
{
    public static final MessageType RESPONSE_TYPE = MessageType.OPPONENT_OFFLINE;
    public static final String DEFAULT_MESSAGE = "OPPONENT IS OFFLINE";

    public OpponentOfflineException()
    {
        super(DEFAULT_MESSAGE);
    }

    public OpponentOfflineException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }


}
