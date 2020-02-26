package server.challenges.timers;

import server.challenges.ChallengesManager;

import java.util.TimerTask;

public class ChallengeTimeOut extends TimerTask
{
    private String from;
    private String to;

    public ChallengeTimeOut(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    @Override
    public void run()
    {
        ChallengesManager.expireChallenge(from, to);
    }
}
