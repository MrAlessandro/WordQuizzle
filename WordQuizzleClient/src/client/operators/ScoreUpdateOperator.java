package client.operators;

public class ScoreUpdateOperator implements Runnable
{
    private String friend;
    private int newScore;

    public ScoreUpdateOperator(String friend, int newScore)
    {
        this.friend = friend;
        this.newScore = newScore;
    }

    @Override
    public void run()
    {

    }
}
