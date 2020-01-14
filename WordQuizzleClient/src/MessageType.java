import java.util.HashMap;

enum MessageType
{
    // Request messages
    LOG_IN(1),
    LOG_OUT(2),
    ADD_FRIEND(3),
    FRIENDS_LIST(4),
    CHALLENGE(5),

    // Response messages
    OK(6),
    OUR_FRIENDS_LIST(7),
    USERNAME_USED(8),
    ALREADY_FRIENDS(9),
    USERNAME_UNKNOWN(10);

    private int value;
    private static HashMap Map = new HashMap<>();

    static
    {
        for (MessageType type : MessageType.values()) {
            Map.put(type.value, type);
        }
    }

    MessageType(int value)
    {
        this.value = value;
    }

    public static MessageType valueOf(int type) {
        return (MessageType) Map.get(type);
    }

    public int getValue() {
        return value;
    }

}


