package server.challenges.challenge;

public class ChallengeReport
{
    public String winner;
    public int applicantProgress;
    public int applicantScore;
    public int opponentProgress;
    public int opponentScore;

    public ChallengeReport(String winner, int applicantProgress, int applicantScore, int opponentProgress, int opponentScore)
    {
        this.winner = winner;
        this.applicantProgress = applicantProgress;
        this.applicantScore = applicantScore;
        this.opponentProgress = opponentProgress;
        this.opponentScore = opponentScore;
    }
}
