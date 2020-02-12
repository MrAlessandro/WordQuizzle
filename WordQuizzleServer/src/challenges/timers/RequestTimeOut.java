package challenges.timers;

import challenges.ChallengesManager;
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
        System.out.println("Timer relative to challenge request from \"" + requestFrom + "\" to \"" + requestTo + "\" has expired");
        try
        {
            UsersManager.cancelChallengeRequest(this.requestFrom, this.requestTo, true);
        } catch (UnexpectedMessageException ignore)
        {}
    }

    public boolean isRelativeTo(String requestFrom, String requestTo)
    {
        return this.requestFrom.equals(requestFrom) && this.requestTo.equals(requestTo);
    }
}
