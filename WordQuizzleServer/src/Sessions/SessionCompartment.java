package Sessions;

import Exceptions.SessionsArchiveInconsistanceException;
import Messages.Message;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class SessionCompartment
{
    private ReentrantReadWriteLock compartmentLock;
    private Session session;

    protected SessionCompartment()
    {
        this.compartmentLock = new ReentrantReadWriteLock();
        this.session = null;
    }

    protected void recordSession(String username) throws SessionsArchiveInconsistanceException
    {
        Session toStore = new Session(username);
        boolean testLock = this.compartmentLock.writeLock().tryLock();
        if (testLock)
        {
            this.session = toStore;
            this.compartmentLock.writeLock().unlock();
        }
        else
            throw new SessionsArchiveInconsistanceException("");
    }

    protected void recordSession(String username, LinkedList<Message> backLog) throws SessionsArchiveInconsistanceException
    {
        Session toStore = new Session(username, backLog);
        boolean testLock = this.compartmentLock.writeLock().tryLock();
        if (testLock)
        {
            this.session = toStore;
            this.compartmentLock.writeLock().unlock();
        }
        else
            throw new SessionsArchiveInconsistanceException("");
    }

    protected Message getPendingSessionMessage() throws SessionsArchiveInconsistanceException
    {
        Message retMessage;
        this.compartmentLock.writeLock().lock();
        if (this.session == null)
        {
            this.compartmentLock.writeLock().unlock();
            throw new SessionsArchiveInconsistanceException("");
        }

        retMessage = this.session.getPendingMessage();

        this.compartmentLock.writeLock().unlock();

        return retMessage;
    }

    protected void storeSessionPendingMessage(Message message) throws SessionsArchiveInconsistanceException
    {
        boolean check;

        this.compartmentLock.writeLock().lock();
        if (this.session == null)
        {
            this.compartmentLock.writeLock().unlock();
            throw new SessionsArchiveInconsistanceException("");
        }

        this.session.storePendingMessage(message);

        this.compartmentLock.writeLock().unlock();
    }
}
