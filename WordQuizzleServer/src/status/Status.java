package status;

import java.util.HashMap;

public enum Status
{
    SUCCESS((short) 1, "SUCCESS"),
    FAIL((short) 2, "FAIL"),
    USER_UNKNOWN ((short) 11, "USER UNKNOWN"),
    WRONG_PASSWORD ((short) 12, "WRONG PASSWORD"),
    USER1_UNKNOWN ((short) 5, "USER 1 UNKNOWN"),
    USER2_UNKNOWN ((short) 6, "USER 2 UNKNOWN"),
    FRIENDSHIP_ALREADY_EXISTS ((short) 7, "FRIENDSHIP ALREADY EXISTS"),
    LOGGED ((short) 8, "LOGGED");

    private short value;
    private String name;
    private static HashMap Map = new HashMap<>();

    static
    {
        for (Status type : Status.values())
        {
            Map.put(type.value, type);
        }
    }

    Status(short value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public static Status valueOf(short type)
    {
        return (Status) Map.get(type);
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
