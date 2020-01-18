package messages;

import java.util.HashMap;

public enum MessageType
{
    // Request messages
    LOG_IN((short) 1, "LogIn"),
    LOG_OUT((short) 2, "LogOut"),
    ADD_FRIEND((short) 3, "AddFriend"),
    FRIENDS_LIST((short) 4, "FriendList"),
    CHALLENGE((short) 5, "Challenge"),

    // Response messages
    OK((short) 6, "Ok"),
    YOUR_FRIENDS_LIST((short) 7, "YourFriendList"),
    USERNAME_USED((short) 8, "UsernameUsed"),
    ALREADY_FRIENDS((short) 9, "AlreadyFriends"),
    USERNAME_UNKNOWN((short) 10, "UsernameUnknown"),
    PASSWORD_WRONG((short) 11, "PasswordWrong");

    private short value;
    private String name;
    private static HashMap Map = new HashMap<>();

    static
    {
        for (MessageType type : MessageType.values()) {
            Map.put(type.value, type);
        }
    }

    MessageType(short value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public static MessageType valueOf(short type)
    {
        return (MessageType) Map.get(type);
    }

    public short getValue()
    {
        return value;
    }

    public String toString()
    {
        return this.name;
    }
}


