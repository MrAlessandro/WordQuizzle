package server.challenges.challenge;


import server.challenges.ChallengesManager;
import server.challenges.timers.ChallengeTimeOut;
import server.constants.ServerConstants;

import java.util.concurrent.atomic.AtomicInteger;

public class Challenge
{
    private String applicant;
    private String opponent;
    private String[] toTranslate;
    private String[][] translated;
    private AtomicInteger applicantProgress;
    private AtomicInteger opponentProgress;
    private AtomicInteger applicantScore;
    private AtomicInteger opponentScore;
    private ChallengeTimeOut challengeTimeOut;

    public Challenge(String applicant, String opponent, String[] toTranslate, String[][] translated)
    {
        // Set the players relative to this challenge and initialize their progresses
        this.applicant = applicant;
        this.opponent = opponent;
        this.toTranslate = toTranslate;
        this.translated = translated;
        this.applicantProgress = new AtomicInteger(0);
        this.opponentProgress = new AtomicInteger(0);
        this.applicantScore = new AtomicInteger(0);
        this.opponentScore = new AtomicInteger(0);

        this.challengeTimeOut = new ChallengeTimeOut(applicant, opponent);
        ChallengesManager.TIMER.schedule(challengeTimeOut, ServerConstants.CHALLENGE_DURATION_SECONDS * 1000);
    }

    public String getFirstWord()
    {
        return toTranslate[0];
    }

    public ChallengeCheckPoint progress(String player, String translation)
    {
        boolean correctTranslation = false;
        String nextWord;

        if (this.opponent.equals(player))
        {
            for (int i = 0; i < this.translated[this.opponentProgress.get()].length; i++)
            {
                if (this.translated[this.opponentProgress.get()][i] != null && this.translated[this.opponentProgress.get()][i].contains(translation))
                {
                     this.opponentScore.addAndGet(ServerConstants.RIGHT_TRANSLATION_SCORE);
                    correctTranslation = true;
                    break;
                }
            }

            if (!correctTranslation)
                this.opponentScore.addAndGet(ServerConstants.WRONG_TRANSLATION_SCORE);


            if (this.opponentProgress.get() < this.toTranslate.length - 1)
                nextWord = this.toTranslate[this.opponentProgress.incrementAndGet()];
            else
                nextWord = null;
        }
        else
        {
            for (int i = 0; i < this.translated[this.applicantProgress.get()].length; i++)
            {
                if (this.translated[this.applicantProgress.get()][i] != null && this.translated[this.applicantProgress.get()][i].contains(translation))
                {
                    this.applicantScore.addAndGet(ServerConstants.RIGHT_TRANSLATION_SCORE);
                    correctTranslation = true;
                    break;
                }
            }

            if (!correctTranslation)
                this.applicantScore.addAndGet(ServerConstants.WRONG_TRANSLATION_SCORE);

            if (this.applicantProgress.get() < this.toTranslate.length - 1)
                nextWord = this.toTranslate[this.applicantProgress.incrementAndGet()];
            else
                nextWord = null;
        }

        return new ChallengeCheckPoint(correctTranslation, nextWord);
    }

    public boolean isOver()
    {
        return (this.applicantProgress.get() == ServerConstants.CHALLENGE_WORDS_QUANTITY - 1) &&
                (this.opponentProgress.get() == ServerConstants.CHALLENGE_WORDS_QUANTITY - 1);
    }

    public ChallengeReport getResults()
    {
        String winner;

        if (this.applicantScore.get() > this.opponentScore.get())
            winner = this.applicant;
        else if (this.opponentScore.get() > this.applicantScore.get())
            winner = this.opponent;
        else
            winner = "";

        return new ChallengeReport(winner, this.applicantProgress.get(), this.applicantScore.get(),
                this.opponentProgress.get(), this.opponentScore.get());
    }

    public void stopTimer()
    {
        this.challengeTimeOut.cancel();
    }
}
