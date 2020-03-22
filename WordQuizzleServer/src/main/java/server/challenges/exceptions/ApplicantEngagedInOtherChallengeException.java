package server.challenges.exceptions;

import commons.exceptions.CommunicableException;
import commons.messages.MessageType;

public class ApplicantEngagedInOtherChallengeException extends CommunicableException
{
    private static final MessageType RESPONSE_TYPE = MessageType.UNEXPECTED_MESSAGE;
    private static final String DEFAULT_MESSAGE = "APPLICANT ENGAGED IN OTHER CHALLENGE";

    public ApplicantEngagedInOtherChallengeException()
    {
        super(DEFAULT_MESSAGE);
    }

    public ApplicantEngagedInOtherChallengeException(String message)
    {
        super(message);
    }

    @Override
    public MessageType getResponseType()
    {
        return RESPONSE_TYPE;
    }
}
