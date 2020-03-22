package server.requests.friendship;

import server.settings.ServerConstants;
import server.requests.friendship.exceptions.FriendshipRequestAlreadyReceived;
import server.requests.friendship.exceptions.FriendshipRequestAlreadySent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FriendshipRequestsManager
{
    private static ConcurrentHashMap<String, Set<String>> friendshipRequestsArchive;

    public static void setUp()
    {
        friendshipRequestsArchive = new ConcurrentHashMap<>(ServerConstants.FRIENDSHIP_REQUESTS_ARCHIVE_INITIAL_SIZE);
    }

    public static void recordFriendshipRequest(String from, String to) throws FriendshipRequestAlreadyReceived, FriendshipRequestAlreadySent
    {
        final boolean[] alreadyReceived = {false};
        final boolean[] alreadySent = {false};

        // Check if applicant has already received a friendship request from the receiver
        friendshipRequestsArchive.computeIfPresent(from, (key, value) -> {
            if (value.contains(to))
                alreadyReceived[0] = true;

            return value;
        });
        if (alreadyReceived[0])
            throw new FriendshipRequestAlreadyReceived();

        // Add the request if it is not already present
        friendshipRequestsArchive.compute(to, (key, value) -> {
            if (value == null)
            {
                HashSet<String> requests = new HashSet<>(10);
                requests.add(from);
                return requests;
            }
            else
            {
                alreadySent[0] = !value.add(from);
                return value;
            }
        });

        // Check if receiver has already received a friendship request from the applicant
        if (alreadySent[0])
            throw new FriendshipRequestAlreadySent();
    }

    public static boolean discardFriendshipRequest(String from, String to)
    {
        final boolean[] removed = {false};

        friendshipRequestsArchive.computeIfPresent(to, (key, value) -> {
            if (value.remove(from))
                removed[0] = true;

            if (value.size() > 0)
                return value;
            else
                return null;
        });

        return removed[0];
    }
}
