package server.challenges.reports;

public class ChallengeReport
{
    public String player;
    public int winStatus;
    public int challengeProgress;
    public int scoreGain;

    public ChallengeReport(String player, int winStatus, int challengeProgress, int scoreGain)
    {
        this.player = player;
        this.winStatus = winStatus;
        this.challengeProgress = challengeProgress;
        this.scoreGain = scoreGain;
    }
}
