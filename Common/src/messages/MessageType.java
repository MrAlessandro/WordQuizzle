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
    CONFIRM_FRIENDSHIP((short) 6, "ConfirmFriendship"),

    // Response messages
    OK((short) 7, "Ok"),
    YOUR_FRIENDS_LIST((short) 8, "YourFriendList"),
    USERNAME_USED((short) 9, "UsernameUsed"),
    ALREADY_FRIENDS((short) 10, "AlreadyFriends"),
    USERNAME_UNKNOWN((short) 11, "UsernameUnknown"),
    PASSWORD_WRONG((short) 12, "PasswordWrong"),
    INVALID_MESSAGE((short) 13, "InvalidMessage"),
    UNEXPECTED_MESSAGE((short) 14, "UnexpectedMessage");


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


