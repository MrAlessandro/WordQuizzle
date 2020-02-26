package server.challenges.timers;

import messages.exceptions.UnexpectedMessageException;

import server.users.UsersManager;
import java.util.TimerTask;

public class RequestTimeOut extends TimerTask
{
    private String requestFrom;
    private String requestTo;

    public RequestTimeOut(String requestFrom, String requestTo)
    {
        super();
        this.requestFrom = requestFrom;
        this.requestTo = requestTo;
    }

    @Override
    public void run()
    {
        try
        {
            UsersManager.cancelChallengeRequest(this.requestFrom, this.requestTo, true);
        } catch (UnexpectedMessageException ignore)
        {}
    }

    public boolean isRelativeTo(String requestFrom, String requestTo)
    {
        return (this.requestFrom.equals(requestFrom) && this.requestTo.equals(requestTo));
    }

    @Override
    public int hashCode()
    {
        return this.requestFrom.hashCode() ^ this.requestTo.hashCode();
    }

    @Override
    public boolean equals(Object compare)
    {
        return (compare instanceof RequestTimeOut) &&
                this.requestFrom.equals(((RequestTimeOut) compare).requestFrom) &&
                this.requestTo.equals(((RequestTimeOut) compare).requestTo);
    }
}
