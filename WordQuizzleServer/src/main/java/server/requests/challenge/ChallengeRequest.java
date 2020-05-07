package server.requests.challenge;

public class ChallengeRequest implements Runnable
{
    public String from;
    public String to;

    private Runnable timeoutOperation;

    public ChallengeRequest(String from, String to, Runnable timeoutOperation)
    {
        super();

        this.from = from;
        this.to = to;
        this.timeoutOperation = timeoutOperation;
    }

    @Override
    public void run()
    {
        timeoutOperation.run();
    }
}
