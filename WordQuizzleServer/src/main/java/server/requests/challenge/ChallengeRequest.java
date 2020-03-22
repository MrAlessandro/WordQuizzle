package server.requests.challenge;

import java.util.TimerTask;

public class ChallengeRequest extends TimerTask
{
    public String from;
    public String to;

    public ChallengeRequest(String from, String to)
    {
        super();

        this.from = from;
        this.to = to;
    }

    @Override
    public void run()
    {
        ChallengeRequestsManager.expireChallengeRequest(from, to);
    }
}
