package Messages;

import org.json.simple.JSONObject;

public class Message
{
    protected MessageType Type;
    protected String Field1;
    protected String Field2;

    public Message(MessageType type, String field1, String field2)
    {
        this.Type = type;
        this.Field1 = field1;
        this.Field2 = field2;
    }

    public JSONObject JSONserialize()
    {
        JSONObject retValue = new JSONObject();

        retValue.put("Type", this.Type.getValue());

        if (this.Field1 != null)
        {
            retValue.put("Field1", this.Field1);

            if (this.Field2 != null)
                retValue.put("Field2", this.Field2);
        }

        return retValue;
    }

    @Override
    public String toString()
    {
        if (this.Field1 == null && this.Field2 == null)
            return "Type: " + Type.toString() + ", NO CONTENTS";
        else if (this.Field1 != null && this.Field2 == null)
            return "Type: " + Type.toString() + ", Field1: " + this.Field1;
        else
            return "Type: " + Type.toString() + ", Field1: " + this.Field1 + ", Field2: " + this.Field2;
    }
}
