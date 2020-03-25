package server.challenges.reports;

public abstract class ChallengeReportDelegation implements Runnable
{
    private ChallengeReport fromChallengeReport = null;
    private ChallengeReport toChallengeReport = null;

    @Override
    public abstract void run();

    public ChallengeReport getFromChallengeReport()
    {
        return fromChallengeReport;
    }

    public ChallengeReport getToChallengeReport()
    {
        return toChallengeReport;
    }

    public void setFromChallengeReport(ChallengeReport fromChallengeReport)
    {
        this.fromChallengeReport = fromChallengeReport;
    }

    public void setToChallengeReport(ChallengeReport toChallengeReport)
    {
        this.toChallengeReport = toChallengeReport;
    }
}
