package Messages;

import java.util.HashMap;

public enum MessageType
{
    // Request messages
    LOG_IN(1, "LogIn"),
    LOG_OUT(2, "LogOut"),
    ADD_FRIEND(3, "AddFriend"),
    FRIENDS_LIST(4, "FriendList"),
    CHALLENGE(5, "Challenge"),

    // Response messages
    OK(6, "Ok"),
    YOUR_FRIENDS_LIST(7, "YourFriendList"),
    USERNAME_USED(8, "UsernameUsed"),
    ALREADY_FRIENDS(9, "AlreadyFriends"),
    USERNAME_UNKNOWN(10, "UsernameUnknown"),
    PASSWORD_WRONG(11, "PasswordWrong");

    private int value;
    private String name;
    private static HashMap Map = new HashMap<>();

    static
    {
        for (MessageType type : MessageType.values()) {
            Map.put(type.value, type);
        }
    }

    MessageType(int value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public static MessageType valueOf(int type) {
        return (MessageType) Map.get(type);
    }

    public int getValue() {
        return value;
    }

    public String toString()
    {
        return this.name;
    }
}


