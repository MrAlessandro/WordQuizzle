package Messages;

import org.json.simple.JSONObject;

public class Field
{
    private short length;
    private char[] body;

    protected Field(char[] content)
    {
        this.length = (short) content.length;
        this.body = content;
    }

    public char[] getBody()
    {
        return this.body;
    }

    protected JSONObject JSONserialize()
    {
        JSONObject serializedThis = new JSONObject();
        serializedThis.put("Length", length);
        serializedThis.put("Body", String.valueOf(this.body));
        return serializedThis;
    }

    protected static Field JSONdeserialize(JSONObject serialized)
    {
        String gotBody = (String) serialized.get("Body");
        return new Field(gotBody.toCharArray());
    }
}
