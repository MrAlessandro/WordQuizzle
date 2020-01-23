package sessions;

import messages.Message;
import sessions.exceptions.SessionsArchiveInconsistanceException;
import util.Constants;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class SessionsManager {
    private static final SessionsManager INSTANCE = new SessionsManager();
    private static final ConcurrentHashMap<String, Session> SESSIONS_ARCHIVE = new ConcurrentHashMap<>(Constants.SessionMapSize);

    private SessionsManager()
    {
    }

    protected static void openSession(String username) throws SessionsArchiveInconsistanceException
    {
        Session checkDuplicates;
        Session opened = new Session(username);

        checkDuplicates = SESSIONS_ARCHIVE.put(username, opened);
        if (checkDuplicates != null)
            throw new SessionsArchiveInconsistanceException("Attempt to insert a duplicate session");

    }

    public static void openSession(String username, Collection<Message> backLog) throws SessionsArchiveInconsistanceException
    {
        Session checkDuplicates;
        Session opened = new Session(username, backLog);

        checkDuplicates = SESSIONS_ARCHIVE.put(username, opened);
        if (checkDuplicates != null)
            throw new SessionsArchiveInconsistanceException("Attempt to insert a duplicate session");

    }

    public static void closeSession(String username) throws SessionsArchiveInconsistanceException
    {
        Session taken = SESSIONS_ARCHIVE.remove(username);
        if (taken == null)
            throw new SessionsArchiveInconsistanceException("Attempt to close a not existing session");

    }

    public static Message retrieveMessage(String username) throws SessionsArchiveInconsistanceException
    {
        Session taken = SESSIONS_ARCHIVE.get(username);
        if (taken == null)
            throw new SessionsArchiveInconsistanceException("Attempt to get a pending message from a not existing session");

        return taken.retrieveMessage();
    }

    public static void appendMessage(String username, Message message) throws SessionsArchiveInconsistanceException
    {
        Session taken = SESSIONS_ARCHIVE.get(username);
        if (taken == null)
            throw new SessionsArchiveInconsistanceException("Attempt to store a pending message in a not existing session");

        taken.appendMessage(message);
    }

    public static void prependMessage(String username, Message message) throws SessionsArchiveInconsistanceException
    {
        Session taken = SESSIONS_ARCHIVE.get(username);
        if (taken == null)
            throw new SessionsArchiveInconsistanceException("Attempt to store a pending message in a not existing session");

        taken.prependMessage(message);
    }

}