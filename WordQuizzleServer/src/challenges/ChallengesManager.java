package challenges;

import challenges.timers.RequestTimeOut;
import server.constants.ServerConstants;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengesManager
{
    private static final ChallengesManager INSTANCE = new ChallengesManager();
    private static final Set<RequestTimeOut> TIMEOUT_COLLECTION = ConcurrentHashMap.newKeySet();
    private static final Timer TIMER = new Timer();

    private ChallengesManager()
    {}

    public static void scheduleRequestTimeOut(String requestFrom, String requestTo)
    {
        RequestTimeOut timeOut = new RequestTimeOut(requestFrom, requestTo);

        boolean check = TIMEOUT_COLLECTION.add(timeOut);
        if (!check)
            throw new Error("Timers storing inconsistency");

        TIMER.schedule(timeOut, ServerConstants.CHALLENGE_REQUEST_TIMEOUT);
    }

    public static void quitScheduledTimeOut(String requestFrom, String requestTo)
    {
        boolean check = false;

        Iterator<RequestTimeOut> iterator = TIMEOUT_COLLECTION.iterator();
        while (iterator.hasNext())
        {
            RequestTimeOut current = iterator.next();
            if (current.isRelativeTo(requestFrom, requestTo))
            {
                check = true;
                current.cancel();
                iterator.remove();
            }
        }

        if (!check)
            throw new Error("Timers storing inconsistency");
    }

    public static void dequeueTimeOut(String requestFrom, String requestTo)
    {
        boolean check = false;

        Iterator<RequestTimeOut> iterator = TIMEOUT_COLLECTION.iterator();
        while (iterator.hasNext())
        {
            RequestTimeOut current = iterator.next();
            if (current.isRelativeTo(requestFrom, requestTo))
            {
                check = true;
                iterator.remove();
            }
        }

        if (!check)
            throw new Error("Timers storing inconsistency");
    }
}
