package server.challenges.challenge;

public class ChallengeCheckPoint
{
    public boolean correct;
    public String nextWord;

    public ChallengeCheckPoint(boolean correct, String nextWord)
    {
        this.correct = correct;
        this.nextWord = nextWord;
    }
}
