package server.challenges.challenge;

import server.constants.ServerConstants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class Challenge
{
    private String applicant;
    private String opponent;
    private String[] toTranslate;
    private String[] translated;
    private int applicantProgress;
    private int opponentProgress;

    public Challenge(String applicant, String opponent, String[] toTranslate, String[] translated)
    {
        // Set the players relative to this challenge and initialize their progresses
        this.applicant = applicant;
        this.opponent = opponent;
        this.toTranslate = toTranslate;
        this.translated = translated;
        this.applicantProgress = 0;
        this.opponentProgress = 0;
    }

    public String getNextWordForApplicant()
    {
        return toTranslate[applicantProgress++];
    }

    public String getNextWordForOpponent()
    {
        return toTranslate[opponentProgress++];
    }


    @Override
    public int hashCode()
    {
        return this.applicant.hashCode() ^ this.opponent.hashCode();
    }

    @Override
    public boolean equals(Object compare)
    {
        return (compare instanceof Challenge) &&
                this.applicant.equals(((Challenge) compare).applicant) &&
                this.opponent.equals(((Challenge) compare).opponent);
    }

}
