package server.requests.challenge;

import server.settings.ServerConstants;
import server.loggers.Logger;
import server.requests.challenge.exceptions.ReceiverEngagedInOtherChallengeRequestException;
import server.requests.challenge.exceptions.PreviousChallengeRequestReceivedException;
import server.requests.challenge.exceptions.PreviousChallengeRequestSentException;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengeRequestsManager
{
    public static final Timer TIMER = new Timer();

    private static ConcurrentHashMap<String, ChallengeRequest> challengeRequestsArchive;
    private static Logger timerLogger;

    public static void setUp()
    {
        challengeRequestsArchive = new ConcurrentHashMap<>(ServerConstants.CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE);
        timerLogger = new Logger("ChallengeRequestsTimer");
    }

    public static void recordChallengeRequest(String from, String to, Runnable timeoutOperation) throws PreviousChallengeRequestSentException, PreviousChallengeRequestReceivedException, ReceiverEngagedInOtherChallengeRequestException
    {
        ChallengeRequest request = new ChallengeRequest(from, to, () -> {
            expireChallengeRequest(from, to);
            timeoutOperation.run();
        });

        synchronized (ChallengeRequestsManager.class)
        {
            ChallengeRequest previousEngageApplicant = challengeRequestsArchive.putIfAbsent(from, request);
            if (previousEngageApplicant != null)
            {
                if (from.equals(previousEngageApplicant.from))
                    throw new PreviousChallengeRequestSentException("USER \"" + from + "\" HAVE BEEN SENT PREVIOUS CHALLENGE REQUEST TO \"" + previousEngageApplicant.to + "\" STILL WAITING FOR REPLY");
                else
                    throw new PreviousChallengeRequestReceivedException("USER \"" + from + "\" HAVE BEEN RECEIVED PREVIOUS CHALLENGE REQUEST FROM \"" + previousEngageApplicant.from + "\" STILL WAITING FOR REPLY");
            }

            ChallengeRequest previousEngageReceiver = challengeRequestsArchive.putIfAbsent(to, request);
            if (previousEngageReceiver != null)
            {
                challengeRequestsArchive.remove(from, request);
                throw new ReceiverEngagedInOtherChallengeRequestException("USER \"" + to + "\" IS ENGAGED IN ANOTHER CHALLENGE REQUEST");
            }
        }

        // Schedule the request timeout
        TIMER.schedule(request, ServerConstants.CHALLENGE_REQUEST_TIMEOUT);
        timerLogger.println("Challenge request between \"" + from + "\" and \"" + to + "\" has been scheduled.");
    }

    private static void expireChallengeRequest(String from, String to)
    {
        ChallengeRequest requestFrom;
        ChallengeRequest requestTo;

        synchronized (ChallengeRequestsManager.class)
        {
            // Remove challenge request related to applicant user from the archive
            requestFrom = challengeRequestsArchive.remove(from);
            if (requestFrom == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Remove challenge request related to receiver user from the archive
            requestTo = challengeRequestsArchive.remove(to);
            if (requestTo == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Requests removed must be the same
            if (requestFrom != requestTo)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");
        }

        timerLogger.println("Challenge request between \"" + from + "\" and \"" + to + "\" has been expired.");
    }

    public static boolean discardChallengeRequest(String from, String to)
    {
        ChallengeRequest requestFrom;
        ChallengeRequest requestTo;

        synchronized (ChallengeRequestsManager.class)
        {
            // Remove challenge request related to applicant user from the archive
            requestFrom = challengeRequestsArchive.remove(from);
            if (requestFrom == null)
                return false;

            // Remove challenge request related to receiver user from the archive
            requestTo = challengeRequestsArchive.remove(to);
            if (requestTo == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Requests removed must be the same
            if (requestFrom != requestTo)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");
        }

        // Cancel the timeout related to the request
        requestFrom.cancel();
        timerLogger.println("Challenge request between \"" + from + "\" and \"" + to + "\" has been discarded.");

        return true;
    }

    public static String cancelChallengeRequest(String username)
    {
        ChallengeRequest request;
        ChallengeRequest consequentialRequest;

        synchronized (ChallengeRequestsManager.class)
        {
            // Remove challenge request related to given user from the archive
            request = challengeRequestsArchive.remove(username);
            if (request == null)
                return null;

            // Remove challenge request related to other user engaged from the archive
            if (username.equals(request.from))
                consequentialRequest = challengeRequestsArchive.remove(request.to);
            else if (username.equals(request.to))
                consequentialRequest = challengeRequestsArchive.remove(request.from);
            else
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Check consistency
            if (consequentialRequest == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");
            if (request != consequentialRequest)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");
        }

        // Cancel the timeout related to the request
        request.cancel();
        timerLogger.println("Challenge request between \"" + request.from + "\" and \"" + request.to + "\" has been canceled.");

        // Return the username of the other user engaged in the request
        return username.equals(request.from) ? request.to : request.from;
    }
}
