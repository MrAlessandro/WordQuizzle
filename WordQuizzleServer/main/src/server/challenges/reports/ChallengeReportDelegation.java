package server.challenges.reports;


public class ChallengeReportDelegation
{
    private ChallengeReport fromChallengeReport = null;
    private ChallengeReport toChallengeReport = null;

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
