package challenges.timers;

import messages.exceptions.UnexpectedMessageException;
import server.users.UsersManager;

import java.nio.channels.Selector;
import java.util.TimerTask;

public class RequestTimeOut extends TimerTask
{
    private String requestFrom;
    private String requestTo;
    private Selector toWake;

    public RequestTimeOut(String requestFrom, String requestTo, Selector toWake)
    {
        super();
        this.requestFrom = requestFrom;
        this.requestTo = requestTo;
        this.toWake = toWake;
    }

    @Override
    public void run()
    {
        System.out.println("Timer relative to challenge request from \"" + requestFrom + "\" to \"" + requestTo + "\" has expired");
        try
        {
            UsersManager.cancelChallengeRequest(this.requestFrom, this.requestTo, true);
            toWake.wakeup();
        } catch (UnexpectedMessageException ignore)
        {}
    }

    public boolean isRelativeTo(String requestFrom, String requestTo)
    {
        return this.requestFrom.equals(requestFrom) && this.requestTo.equals(requestTo);
    }
}
