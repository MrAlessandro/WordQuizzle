import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class UserMap
{
    private static final LinkedList<User>[] Table = new LinkedList[Constants.UserMapSize];
    private static final ReentrantReadWriteLock[] Keychain = new ReentrantReadWriteLock[Constants.UserMapSize/Constants.UserMapBunchSize];
    private static long Load;

    private int hash(String toHash)
    {
        return Math.abs(toHash.hashCode() % Constants.UserMapSize);
    }

    public UserMap()
    {
        Load = 0;

        for (int i = 0; i < Constants.UserMapSize/Constants.UserMapBunchSize; i++)
        {
            Table[i] = new LinkedList<User>();
            Keychain[i] = new ReentrantReadWriteLock();
        }

        for (int i = Constants.UserMapSize/Constants.UserMapBunchSize; i < Constants.UserMapSize; i++)
        {
            Table[i] = new LinkedList<User>();
        }

    }

    // It generates an instance of User with given username and password,
    // then it insert it in a thread-safe way within the users table
    public boolean insert(String username, Password passwd)
    {
        User newUser = new User(username, passwd);
        int index = hash(username);
        boolean retValue = true;

        Keychain[index/Constants.UserMapBunchSize].writeLock().lock();
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
        Keychain[index/Constants.UserMapBunchSize].writeLock().unlock();

        return retValue;
    }

    // It insert a given User instance within the users table in a not thread-safe way
    protected void put(User user)
    {
        int index = hash(user.getUserName());
        boolean retValue = true;

        Table[index].addFirst(user);
        Load ++;
    }

    public boolean addFriendship(String userName1, String userName2) throws InconsistentRelationshipException
    {
        int index1 = hash(userName1);
        int index2 = hash(userName2);
        int lockIndex1 = index1/Constants.UserMapBunchSize;
        int lockIndex2 = index1/Constants.UserMapBunchSize;
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

    public JSONArray JSONserialize()
    {
        JSONArray mapArray = new JSONArray();

        for (int i = 0; i < Constants.UserMapSize/Constants.UserMapBunchSize; i++)
        {
            Keychain[i].readLock().lock();
            for (int j = 0; j < Constants.UserMapBunchSize; j++)
            {
                for( User user : Table[i*Constants.UserMapBunchSize+j])
                {
                    mapArray.add(user.JSONserilize());
                }
            }
            Keychain[i].readLock().unlock();
        }

        return mapArray;
    }

    public void JSONdeserialize(String json) throws ParseException
    {
        JSONParser parser = new JSONParser();
        JSONArray mapArray = (JSONArray) parser.parse(json);
        Iterator<JSONObject> iterator = mapArray.iterator();
        while (iterator.hasNext())
        {
            JSONObject currentUser = iterator.next();
            String currentUsername = (String) currentUser.get("UserName");
            Long currentScore = (Long) currentUser.get("Score");
            JSONObject currentPassword = (JSONObject) currentUser.get("Password");
            JSONArray currentFriendList = (JSONArray) currentUser.get("Friends");

            Password password = new Password((String) currentPassword.get("Password"), ((String) currentPassword.get("Salt")).getBytes());
            LinkedList<String> friendList = new LinkedList<String>();

            Iterator<String> iter2 = currentFriendList.iterator();
            while (iter2.hasNext())
            {
                String friend = iter2.next();
                friendList.addFirst(friend);
            }

            put(new User(currentUsername, password, currentScore.intValue(), friendList));
        }
    }

    public void print()
    {
        int counter = 1;
        for (int i = 0; i < Constants.UserMapSize/Constants.UserMapBunchSize; i++)
        {
            Keychain[i].readLock().lock();
            for (int j = 0; j < Constants.UserMapBunchSize; j++)
            {
                for( User user : Table[i*Constants.UserMapBunchSize+j])
                {
                    System.out.println("User N° " + counter);
                    System.out.println("    Username: " + user.getUserName());
                    System.out.println("    Password: " + new String(user.getPassword()));
                    System.out.println("    Friends list: ");

                    Iterator<String> iter = user.getFriendListIterator();
                    while (iter.hasNext())
                    {
                        String friend = iter.next();
                        System.out.println("        " + friend);
                    }

                    counter++;
                }
            }
            Keychain[i].readLock().unlock();
        }
    }

}
