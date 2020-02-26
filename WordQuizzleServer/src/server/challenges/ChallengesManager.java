package server.challenges;

import messages.Message;
import messages.MessageType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.challenges.challenge.Challenge;
import server.challenges.challenge.ChallengeCheckPoint;
import server.challenges.challenge.ChallengeReport;
import server.challenges.exceptions.InexistentChallengeException;
import server.challenges.timers.RequestTimeOut;
import server.constants.ServerConstants;
import server.users.UsersManager;
import server.users.exceptions.UnknownUserException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengesManager
{
    private static final ConcurrentHashMap<String, RequestTimeOut> REQUESTS_TIMEOUTS_COLLECTION = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Challenge> CHALLENGES_ARCHIVE = new ConcurrentHashMap<>();
    public static final Timer TIMER = new Timer();

    private ChallengesManager()
    {}

    public static void scheduleRequestTimeOut(String applicant, String opponent)
    {
        RequestTimeOut timeOut = new RequestTimeOut(applicant, opponent);

        RequestTimeOut check = REQUESTS_TIMEOUTS_COLLECTION.put(applicant + ":" + opponent, timeOut);
        if (check != null)
            throw new Error("Challenge requests timers storing inconsistency");

        TIMER.schedule(timeOut, ServerConstants.CHALLENGE_REQUEST_TIMEOUT);
    }

    public static boolean quitScheduledRequestTimeOut(String applicant, String opponent)
    {
        RequestTimeOut taken = REQUESTS_TIMEOUTS_COLLECTION.remove(applicant + ":" + opponent);
        if (taken == null)
            throw new Error("Challenge requests timers storing inconsistency");

        taken.cancel();

        return true;
    }

    public static boolean dequeueScheduledRequestTimeOut(String applicant, String opponent)
    {
        RequestTimeOut taken = REQUESTS_TIMEOUTS_COLLECTION.remove(applicant + ":" + opponent);
        if (taken == null)
            throw new Error("Challenge requests timers storing inconsistency");

        return true;
    }

    public static String registerChallenge(String applicant, String opponent)
    {
        String[] toTranslate;
        String[][] translated;

        // Get italians words from dictionary
        toTranslate = ServerConstants.getBulkOfWords();

        // Translate taken words
        translated = ServerConstants.translateBulkOfWords(toTranslate);

        String challengeKey = applicant + ":" + opponent;
        Challenge challenge = new Challenge(applicant, opponent, toTranslate, translated);
        String firstWord = challenge.getFirstWord();
        CHALLENGES_ARCHIVE.put(challengeKey, challenge);

        return firstWord;
    }

    public static void endChallenge(String applicant, String opponent)
    {
        Challenge challenge = CHALLENGES_ARCHIVE.remove(applicant+ ":" + opponent);
        if (challenge == null)
            throw new Error("Challenges storing inconsistency");
        try
        {
            // Stop timer relative to this challenge
            challenge.stopTimer();

            // Get challenge results
            ChallengeReport report = challenge.getResults();

            Message applicantMessage = new Message(MessageType.CHALLENGE_REPORT, applicant, opponent,
                    report.winner, String.valueOf(report.applicantProgress), String.valueOf(report.applicantScore));
            Message opponentMessage = new Message(MessageType.CHALLENGE_REPORT, applicant, opponent,
                    report.winner, String.valueOf(report.opponentProgress), String.valueOf(report.opponentScore));


            UsersManager.sendMessage(applicant, applicantMessage);
            UsersManager.sendMessage(opponent, opponentMessage);

            UsersManager.updateUserScore(applicant, report.applicantScore);
            UsersManager.updateUserScore(opponent, report.opponentScore);

            // Remove challenge from archive
            CHALLENGES_ARCHIVE.remove(applicant + ":" + opponent);

            // Remove challenge from users system
            UsersManager.quitChallenge(applicant);
            UsersManager.quitChallenge(opponent);
        }
        catch (UnknownUserException e)
        {
            e.printStackTrace();
            throw new Error("Users database inconsistency");
        }
    }

    public static void expireChallenge(String applicant, String opponent)
    {
        Challenge challenge = CHALLENGES_ARCHIVE.remove(applicant+ ":" + opponent);
        if (challenge == null)
            throw new Error("Challenges storing inconsistency");
        try
        {
            // Get challenge results
            ChallengeReport report = challenge.getResults();

            Message applicantMessage = new Message(MessageType.CHALLENGE_TIMEOUT_EXPIRED, applicant, opponent,
                    report.winner, String.valueOf(report.applicantProgress), String.valueOf(report.applicantScore));
            Message opponentMessage = new Message(MessageType.CHALLENGE_TIMEOUT_EXPIRED, applicant, opponent,
                    report.winner, String.valueOf(report.opponentProgress), String.valueOf(report.opponentScore));


            UsersManager.sendMessage(applicant, applicantMessage);
            UsersManager.sendMessage(opponent, opponentMessage);

            UsersManager.updateUserScore(applicant, report.applicantScore);
            UsersManager.updateUserScore(opponent, report.opponentScore);

            // Remove challenge from archive
            CHALLENGES_ARCHIVE.remove(applicant + ":" + opponent);

            // Remove challenge from users system
            UsersManager.quitChallenge(applicant);
            UsersManager.quitChallenge(opponent);
        }
        catch (UnknownUserException e)
        {
            e.printStackTrace();
            throw new Error("Users database inconsistency");
        }
    }

    public static ChallengeReport abortChallenge(String applicant, String opponent)
    {
        Challenge challenge = CHALLENGES_ARCHIVE.remove(applicant+ ":" + opponent);
        if (challenge == null)
            return null;

        // Stop timer relative to this challenge
        challenge.stopTimer();

        // Remove challenge from archive
        CHALLENGES_ARCHIVE.remove(applicant + ":" + opponent);

        return challenge.getResults();
    }

    public static ChallengeCheckPoint checkTranslation(String applicant, String opponent, String currentPlayer, String translation) throws InexistentChallengeException
    {
        ChallengeCheckPoint checkPoint;
        Challenge challenge = CHALLENGES_ARCHIVE.get(applicant + ":" + opponent);
        if (challenge == null)
            throw new InexistentChallengeException();

        checkPoint = challenge.progress(currentPlayer, translation);

        if (challenge.isOver())
            endChallenge(applicant, opponent);

        return checkPoint;
    }
}
