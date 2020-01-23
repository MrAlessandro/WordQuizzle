package oldSessions;

import messages.Message;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;

public class Session {
    private String sessionUser;
    private LinkedList<Message> sessionMessageBuffer;
    private DatagramChannel notificationChannel;

    protected Session(String username) {
        this.sessionUser = username;
        this.sessionMessageBuffer = new LinkedList<>();
    }

    protected Session(String username, LinkedList<Message> backLog) {
        this.sessionUser = username;
        this.sessionMessageBuffer = backLog;
    }

    protected Message getPendingMessage() {
        return this.sessionMessageBuffer.pollFirst();
    }

    protected void storePendingMessage(Message message) {
        this.sessionMessageBuffer.addLast(message);
    }

    protected LinkedList<Message> retrieveBackLogCloseSession() throws IOException
    {
        Message current;
        LinkedList<Message> returnList = new LinkedList<>();

        while ((current = this.sessionMessageBuffer.pollFirst()) != null)
        {
            returnList.addLast(current);
        }

        this.notificationChannel.close();

        return returnList;
    }
}
