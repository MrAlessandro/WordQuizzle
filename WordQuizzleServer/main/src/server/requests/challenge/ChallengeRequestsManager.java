package server.requests.challenge;

import server.settings.ServerConstants;
import commons.loggers.Logger;
import server.requests.challenge.exceptions.ReceiverEngagedInOtherChallengeRequestException;
import server.requests.challenge.exceptions.PreviousChallengeRequestReceivedException;
import server.requests.challenge.exceptions.PreviousChallengeRequestSentException;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChallengeRequestsManager
{
    private ConcurrentHashMap<String, ChallengeRequest> challengeRequestsArchive;
    private ConcurrentHashMap<ChallengeRequest, ScheduledFuture<?>> timeOutsArchive;
    public ScheduledThreadPoolExecutor timer;
    private Logger timerLogger;

    public ChallengeRequestsManager()
    {
        this.challengeRequestsArchive = new ConcurrentHashMap<>(ServerConstants.CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE);
        this.timeOutsArchive = new ConcurrentHashMap<>(128);
        this.timer = new ScheduledThreadPoolExecutor(5);

        try
        {
            if (ServerConstants.LOG_FILES)
                // Create timer logger with related log file
                this.timerLogger = new Logger(ServerConstants.COLORED_LOGS, "ChallengeRequestsTimer", ServerConstants.LOG_FILES_PATH);
            else
                // Create timer logger
                this.timerLogger = new Logger(ServerConstants.COLORED_LOGS);
        }
        catch (IOException e)
        {
            throw new Error("ERROR CREATING LOGGER", e);
        }
    }

    public void recordChallengeRequest(String from, String to, Runnable timeoutOperation) throws PreviousChallengeRequestSentException, PreviousChallengeRequestReceivedException, ReceiverEngagedInOtherChallengeRequestException
    {
        ChallengeRequest request = new ChallengeRequest(from, to, () -> {
            expireChallengeRequest(from, to);
            timeoutOperation.run();
        });

        synchronized (ChallengeRequestsManager.class)
        {
            ChallengeRequest previousEngageApplicant = this.challengeRequestsArchive.putIfAbsent(from, request);
            if (previousEngageApplicant != null)
            {
                if (from.equals(previousEngageApplicant.from))
                    throw new PreviousChallengeRequestSentException("USER \"" + from + "\" HAVE BEEN SENT PREVIOUS CHALLENGE REQUEST TO \"" + previousEngageApplicant.to + "\" STILL WAITING FOR REPLY");
                else
                    throw new PreviousChallengeRequestReceivedException("USER \"" + from + "\" HAVE BEEN RECEIVED PREVIOUS CHALLENGE REQUEST FROM \"" + previousEngageApplicant.from + "\" STILL WAITING FOR REPLY");
            }

            ChallengeRequest previousEngageReceiver = this.challengeRequestsArchive.putIfAbsent(to, request);
            if (previousEngageReceiver != null)
            {
                this.challengeRequestsArchive.remove(from, request);
                throw new ReceiverEngagedInOtherChallengeRequestException("USER \"" + to + "\" IS ENGAGED IN ANOTHER CHALLENGE REQUEST");
            }

            // Schedule the request timeout
            ScheduledFuture<?> scheduledFuture = this.timer.schedule(request, ServerConstants.CHALLENGE_REQUEST_TIMEOUT, TimeUnit.SECONDS);
            this.timeOutsArchive.put(request, scheduledFuture);
            this.timerLogger.println("Challenge request between \"" + from + "\" and \"" + to + "\" has been scheduled.");
        }
    }

    private void expireChallengeRequest(String from, String to)
    {
        ChallengeRequest requestFrom;
        ChallengeRequest requestTo;

        synchronized (ChallengeRequestsManager.class)
        {
            // Remove challenge request related to applicant user from the archive
            requestFrom = this.challengeRequestsArchive.remove(from);
            if (requestFrom == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Remove challenge request related to receiver user from the archive
            requestTo = this.challengeRequestsArchive.remove(to);
            if (requestTo == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Requests removed must be the same
            if (requestFrom != requestTo)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");
        }

        this.timerLogger.println("Challenge request between \"" + from + "\" and \"" + to + "\" has been expired.");
    }

    public boolean discardChallengeRequest(String from, String to)
    {
        ChallengeRequest requestFrom;
        ChallengeRequest requestTo;

        synchronized (ChallengeRequestsManager.class)
        {
            // Remove challenge request related to applicant user from the archive
            requestFrom = this.challengeRequestsArchive.remove(from);
            if (requestFrom == null)
                return false;

            // Remove challenge request related to receiver user from the archive
            requestTo = this.challengeRequestsArchive.remove(to);
            if (requestTo == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Requests removed must be the same
            if (requestFrom != requestTo)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Cancel the timeout related to the request
            ScheduledFuture<?> scheduledFuture = this.timeOutsArchive.remove(requestFrom);
            scheduledFuture.cancel(true);
            this.timerLogger.println("Challenge request between \"" + from + "\" and \"" + to + "\" has been discarded.");
        }

        return true;
    }

    public String cancelChallengeRequest(String username)
    {
        ChallengeRequest request;
        ChallengeRequest consequentialRequest;

        synchronized (ChallengeRequestsManager.class)
        {
            // Remove challenge request related to given user from the archive
            request = this.challengeRequestsArchive.remove(username);
            if (request == null)
                return null;

            // Remove challenge request related to other user engaged from the archive
            if (username.equals(request.from))
                consequentialRequest = this.challengeRequestsArchive.remove(request.to);
            else if (username.equals(request.to))
                consequentialRequest = this.challengeRequestsArchive.remove(request.from);
            else
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Check consistency
            if (consequentialRequest == null)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");
            if (request != consequentialRequest)
                throw new Error("CHALLENGE REQUEST MANAGER INCONSISTENCY");

            // Cancel the timeout related to the request
            ScheduledFuture<?> scheduledFuture = this.timeOutsArchive.remove(request);
            scheduledFuture.cancel(true);
            this.timerLogger.println("Challenge request between \"" + request.from + "\" and \"" + request.to + "\" has been canceled.");
        }

        // Return the username of the other user engaged in the request
        return username.equals(request.from) ? request.to : request.from;
    }
}
