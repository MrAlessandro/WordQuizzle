package messages;

import java.util.HashMap;

public enum MessageType
{
    // Request messages
    LOG_IN((short) 1, "LogIn"),
    LOG_OUT((short) 2, "LogOut"),
    FRIENDS_LIST((short) 4, "FriendList"),

    // Notifications,
    REQUEST_FOR_FRIENDSHIP((short) 100, "RequestForFriend"),
    CONFIRM_FRIENDSHIP((short) 101, "ConfirmFriendship"),
    DECLINE_FRIENDSHIP((short) 102, "DeclineFriendship"),
    FRIENDSHIP_CONFIRMED((short) 104, "FriendshipConfirmed"),
    FRIENDSHIP_DECLINED((short)105, "FriendshipDeclined"),
    CHALLENGE_REQUEST((short) 103, "NotifyChallenge"),

    // Response messages
    OK((short) 7, "Ok"),
    YOUR_FRIENDS_LIST((short) 8, "YourFriendList"),
    USERNAME_USED((short) 9, "UsernameUsed"),
    ALREADY_FRIENDS((short) 10, "AlreadyFriends"),
    USERNAME_UNKNOWN((short) 11, "UsernameUnknown"),
    PASSWORD_WRONG((short) 12, "PasswordWrong"),
    INVALID_MESSAGE_FORMAT((short) 13, "InvalidMessage"),
    UNEXPECTED_MESSAGE((short) 14, "UnexpectedMessage"),
    FRIENDSHIP_REQUEST_ALREADY_SENT ((short) 16, "FriendshipRequestAlreadySent");


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

    public short getValue()
    {
        return value;
    }

    public String toString()
    {
        return this.name;
    }

    public static MessageType valueOf(short type)
    {
        return (MessageType) Map.get(type);
    }

    public static boolean isNotification(MessageType type)
    {
        return type.value >= 100 && type.value <= 200;
    }
}


