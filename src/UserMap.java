import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserMap
{
    private static final int TableSize = 16384;
    private static final short BunchSize = 16;
    private static final LinkedList<User>[] Table = new LinkedList[TableSize];
    private static final ReentrantReadWriteLock[] Keychain = new ReentrantReadWriteLock[TableSize/BunchSize];
    private static long Load;

    private int hash(String toHash)
    {
        return toHash.hashCode() % TableSize;
    }

    public UserMap()
    {
        Load = 0;

        for (int i = 0; i < TableSize/BunchSize; i++)
        {
            Table[i] = new LinkedList<User>();
            Keychain[i] = new ReentrantReadWriteLock();
        }

        for (int i = TableSize/BunchSize; i < TableSize; i++)
        {
            Table[i] = new LinkedList<User>();
        }

    }

    public boolean put(String username, char[] passwd)
    {
        User newUser = new User(username, passwd);
        int index = hash(username);
        boolean retValue = true;

        Keychain[index/BunchSize].writeLock().lock();
        for(User current : Table[index])
        {
            if (current.getUserName().equals(username))
            {
                retValue = false;
                break;
            }
        }

        if (retValue)
        {
            Table[index].addFirst(newUser);
            Load ++;
        }
        Keychain[index/BunchSize].writeLock().unlock();

        return retValue;
    }

    public boolean addFriendship(String userName1, String userName2) throws InconsistentRelationshipException
    {
        int index1 = hash(userName1);
        int index2 = hash(userName2);
        int lockIndex1 = index1/BunchSize;
        int lockIndex2 = index1/BunchSize;
        User user1 = null;
        User user2 = null;
        boolean retValue = false;

        // Lock both (if are different) table partitions
        Keychain[lockIndex1].writeLock().lock();
        if (lockIndex2 != lockIndex1)
            Keychain[lockIndex2].writeLock().lock();

        // Looking for first user
        for (User current1 : Table[index1])
        {
            if (current1.getUserName().equals(userName1))
            {
                user1 = current1;
                break;
            }
        }

        if (user1 != null)
        {
            // Looking for second user
            for (User current2 : Table[index2])
            {
                if (current2.getUserName().equals(userName2))
                {
                    user2 = current2;
                    break;
                }
            }

            if (user2 != null)
            {
                boolean test1;
                boolean test2;
                test1 = user1.addFriend(userName2);
                test2 = user2.addFriend(userName1);


                if (test1 == test2)
                    retValue = test1;
                else
                    throw new InconsistentRelationshipException("Inconsistent relationship between " + userName1 + " and " + userName2);
            }
        }

        Keychain[lockIndex1].writeLock().unlock();
        if (lockIndex2 != lockIndex1)
            Keychain[lockIndex2].writeLock().unlock();

        return retValue;
    }

}
