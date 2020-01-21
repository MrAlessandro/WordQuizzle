package users;

import messages.Message;
import users.exceptions.*;
import util.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class UsersArchive
{
    private static final LinkedList<User>[] Table = new LinkedList[Constants.UserMapSize];
    private static final ReentrantReadWriteLock[] Keychain = new ReentrantReadWriteLock[Constants.UserMapSize/ Constants.UserMapBunchSize];
    private static long Load;

    private int hash(String toHash)
    {
        return Math.abs(toHash.hashCode() % Constants.UserMapSize);
    }

    public UsersArchive()
    {
        Load = 0;

        for (int i = 0; i < Constants.UserMapSize/ Constants.UserMapBunchSize; i++)
        {
            Table[i] = new LinkedList<User>();
            Keychain[i] = new ReentrantReadWriteLock();
        }

        for (int i = Constants.UserMapSize/ Constants.UserMapBunchSize; i < Constants.UserMapSize; i++)
        {
            Table[i] = new LinkedList<User>();
        }

    }

    // It generates an instance of UsersNetwork.User with given username and password,
    // then it insert it in a thread-safe way within the users table
    protected boolean insert(String username, Password passwd) throws NameNotUniqueException
    {
        User newUser = new User(username, passwd);
        int index = hash(username);

        Keychain[index/ Constants.UserMapBunchSize].writeLock().lock();
        for(User current : Table[index])
        {
            if (current.getUserName().equals(username))
            {
                Keychain[index/ Constants.UserMapBunchSize].writeLock().unlock();
                throw new NameNotUniqueException("Username \"" + username + "\" already present in the system");
            }
        }

        Table[index].addFirst(newUser);
        Load ++;

        Keychain[index/ Constants.UserMapBunchSize].writeLock().unlock();

        return true;
    }

    protected boolean checkUserPassword(String username, char[] password) throws UnknownUserException
    {
        int index = hash(username);
        boolean found = false;
        boolean check = false;

        Keychain[index/ Constants.UserMapBunchSize].readLock().lock();
        for(User current : Table[index])
        {
            if (current.getUserName().equals(username))
            {
                check = current.checkPassword(password);
                Keychain[index/ Constants.UserMapBunchSize].readLock().unlock();
                found = true;
                break;
            }
        }
        Keychain[index/ Constants.UserMapBunchSize].readLock().unlock();

        if (!found)
            throw new UnknownUserException("UsersNetwork.User " + username + " does not exist");
        else
            return check;
    }

    protected LinkedList<Message> retrieveUserBackLog(String username) throws UnknownUserException
    {
        int index = hash(username);
        LinkedList<Message> returnList = null;

        Keychain[index/ Constants.UserMapBunchSize].readLock().lock();
        for(User current : Table[index])
        {
            if (current.getUserName().equals(username))
            {
                returnList = current.retrieveBackLog();
                Keychain[index/ Constants.UserMapBunchSize].readLock().unlock();
            }
        }
        Keychain[index/ Constants.UserMapBunchSize].readLock().unlock();

        if (returnList == null)
            throw new UnknownUserException("UsersNetwork.User " + username + " does not exist");
        else
            return returnList;
    }

    protected LinkedList<Message> validateUserPasswordRetrieveBacklog(String username, char[] password) throws UnknownUserException {
        int index = hash(username);
        boolean found = false;
        LinkedList<Message> returnList = null;


        Keychain[index/ Constants.UserMapBunchSize].readLock().lock();
        for(User current : Table[index])
        {
            if (current.getUserName().equals(username))
            {
                found = true;

                if (current.checkPassword(password))
                    returnList = current.retrieveBackLog();

                break;
            }
        }
        Keychain[index/ Constants.UserMapBunchSize].readLock().unlock();

        if (!found)
            throw new UnknownUserException("UsersNetwork.User " + username + " does not exist");
        else
            return returnList;
    }

    // It insert a given UsersNetwork.User instance within the users table in a not thread-safe way
    protected void put(User user)
    {
        int index = hash(user.getUserName());
        boolean retValue = true;

        Table[index].addFirst(user);
        Load ++;
    }

    public boolean addFriendship(String userName1, String userName2) throws InconsistentRelationshipException, UnknownFirstUserException, UnknownSecondUserException, AlreadyExistingRelationshipException
    {
        int index1 = hash(userName1);
        int index2 = hash(userName2);
        int lockIndex1 = index1/ Constants.UserMapBunchSize;
        int lockIndex2 = index1/ Constants.UserMapBunchSize;
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

        if (user1 == null)
        {// If first user does not exist
            Keychain[lockIndex1].writeLock().unlock();
            if (lockIndex2 != lockIndex1)
                Keychain[lockIndex2].writeLock().unlock();

            throw new UnknownFirstUserException("Unknown user \"" + userName1 + "\", impossible to generate the relationship");
        }

        // Looking for second user
        for (User current2 : Table[index2])
        {
            if (current2.getUserName().equals(userName2))
            {
                user2 = current2;
                break;
            }
        }

        if (user2 == null)
        {// If second user does not exist
            Keychain[lockIndex1].writeLock().unlock();
            if (lockIndex2 != lockIndex1)
                Keychain[lockIndex2].writeLock().unlock();

            throw new UnknownSecondUserException("Unknown user \"" + userName2 + "\", impossible to generate the relationship");
        }

        boolean test1;
        boolean test2;
        test1 = user1.addFriend(userName2);
        test2 = user2.addFriend(userName1);

        Keychain[lockIndex1].writeLock().unlock();
        if (lockIndex2 != lockIndex1)
            Keychain[lockIndex2].writeLock().unlock();

        if (test1 && test2)
            return true;
        else if (!test1 && !test2)
            throw new AlreadyExistingRelationshipException("Relationship already exists between \"" + userName1 + "\" and \"" + userName2 +"\"");
        else
            throw new InconsistentRelationshipException("Inconsistent relationship between " + userName1 + " and " + userName2);

    }

    public JSONArray JSONserialize()
    {
        JSONArray mapArray = new JSONArray();

        for (int i = 0; i < Constants.UserMapSize/ Constants.UserMapBunchSize; i++)
        {
            Keychain[i].readLock().lock();
            for (int j = 0; j < Constants.UserMapBunchSize; j++)
            {
                for( User user : Table[i* Constants.UserMapBunchSize+j])
                {
                    mapArray.add(user.JSONserialize());
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
        for (JSONObject currentUser : (Iterable<JSONObject>) mapArray)
        {
            put(User.JSONdeserialize(currentUser));
        }
    }

    public void print()
    {
        int counter = 1;
        for (int i = 0; i < Constants.UserMapSize/ Constants.UserMapBunchSize; i++)
        {
            Keychain[i].readLock().lock();
            for (int j = 0; j < Constants.UserMapBunchSize; j++)
            {
                for( User user : Table[i* Constants.UserMapBunchSize+j])
                {
                    System.out.println("UsersNetwork.User N° " + counter);
                    System.out.println("    Username: " + user.getUserName());
                    System.out.println("    Password: " + new String(user.getPassword()));
                    System.out.println("    Friends list: ");

                    Iterator<String> iter = user.getFriendListIterator();
                    while (iter.hasNext())
                    {
                        String friend = iter.next();
                        System.out.println("        " + friend);
                    }

                    Iterator<Message> iter2 = user.getBackLogMessageIterator();
                    while (iter.hasNext())
                    {
                        Message mex = (Message) iter2.next();
                        System.out.println("        " + mex.toString());
                    }

                    counter++;
                }
            }
            Keychain[i].readLock().unlock();
        }
    }

}
