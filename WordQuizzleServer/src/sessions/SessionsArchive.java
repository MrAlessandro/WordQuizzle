package sessions;

import exceptions.SessionsArchiveInconsistanceException;
import messages.Message;
import util.Constants;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

class SessionsArchive
{
    private static final SessionsArchive instance = new SessionsArchive();
    private static final SessionCompartment[] archive = new SessionCompartment[Constants.SessionMapSize];
    private static final ReentrantLock lastLock = new ReentrantLock();
    private static int last = 0;

    static
    {
        for (int i = 0; i < archive.length; i++)
        {
            archive[i] = new SessionCompartment();
        }
    }

    private SessionsArchive(){}

    protected static SessionsArchive getInstance()
    {
        return instance;
    }

    protected static int recordSession(String username) throws SessionsArchiveInconsistanceException
    {
        int index;

        lastLock.lock();
        index = last;
        archive[last].recordSession(username);
        lastLock.unlock();

        return index;
    }

    protected static int recordSession(String username, LinkedList<Message> backlog) throws SessionsArchiveInconsistanceException
    {
        int index;

        lastLock.lock();
        index = last;
        archive[last].recordSession(username, backlog);
        lastLock.unlock();

        return index;
    }

    protected static Message getSessionPendingMessage(int index) throws SessionsArchiveInconsistanceException
    {
        return archive[index].getPendingSessionMessage();
    }

    protected static void storeSessionPendingMessage(int index, Message message) throws SessionsArchiveInconsistanceException
    {
        archive[index].storeSessionPendingMessage(message);
    }
}
