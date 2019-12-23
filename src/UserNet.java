import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;

public class UserNet
{
    private final static UserNet Net = new UserNet();
    private final static UserMap Map = new UserMap();

    private UserNet() {}

    protected static UserNet getNet()
    {
        return Net;
    }

    protected static boolean registerUser(String userName, String passwd)
    {
        boolean test;

        test = Map.insert(userName, new Password(passwd));

        return test;
    }

    protected static boolean addFriendship(String userName1, String userName2) throws InconsistentRelationshipException
    {
        boolean test;

        test = Map.addFriendship(userName1, userName2);

        return test;
    }

    protected static String backUpNet()
    {
        return Map.JSONserialize().toJSONString();
    }

    protected static void restoreNet()
    {
        try
        {
            byte[] read = Files.readAllBytes(Constants.UserNetBackUpPath);
            String json = new String(read, Charset.defaultCharset());

            JSONParser parser = new JSONParser();

            JSONObject map = (JSONObject) parser.parse(json);
            JSONArray mapArray = (JSONArray) map.get("Map");
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

                Map.put(new User(currentUsername, password, currentScore.intValue(), friendList));
            }
        }
        catch (ParseException | FileNotFoundException e)
        {
            System.err.println("Error parsing server's users database.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected static void printNet()
    {
        Map.print();
    }
}
