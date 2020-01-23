package oldSessions;

import messages.Message;
import oldSessions.exceptions.SessionsArchiveInconsistanceException;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class SessionsManager {
    private static final SessionsManager instance = new SessionsManager();
    private static final ConcurrentHashMap<SocketChannel, Integer> socketTOsession = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> usernameTOsession = new ConcurrentHashMap<>();

    private SessionsManager() {
    }

    protected void openSession(SocketChannel socket, String username) throws SessionsArchiveInconsistanceException
    {
        int index = SessionsArchive.recordSession(username);
        socketTOsession.put(socket, index);
        usernameTOsession.put(username, index);
    }

    protected SessionCompartment getSession(String username) {
        int index = usernameTOsession.get(username);
        return SessionsArchive.getCompartment(index);
    }


    public static void openSession(SocketChannel socket, String username, LinkedList<Message> backLog) throws SessionsArchiveInconsistanceException
    {
        int index = SessionsArchive.recordSession(username, backLog);
        socketTOsession.put(socket, index);
        usernameTOsession.put(username, index);
    }

    public static void closeSession(String username)
    {
        int index = usernameTOsession.get(username);
    }

    public static Message getPendingSessionMessage(SocketChannel socket) throws SessionsArchiveInconsistanceException
    {
        int sessionIndex = socketTOsession.get(socket);
        return SessionsArchive.getSessionPendingMessage(sessionIndex);
    }

    public static void storePendingSessionMessage(String username, Message message) throws SessionsArchiveInconsistanceException
    {
        int sessionIndex = usernameTOsession.get(username);
        SessionsArchive.storeSessionPendingMessage(sessionIndex, message);
    }

}