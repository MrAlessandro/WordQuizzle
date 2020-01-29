package dispatching;

import java.util.HashMap;

public enum OperationType
{
    WRITE ((short) 1, "Read"),
    READ ((short) 2, "Write"),
    CLOSE ((short) 3, "Close");

    private short value;
    private String name;
    private static HashMap Map = new HashMap<>();

    static
    {
        for (OperationType type : OperationType.values()) {
            Map.put(type.value, type);
        }
    }

    OperationType(short value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public static OperationType valueOf(short type)
    {
        return (OperationType) Map.get(type);
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
