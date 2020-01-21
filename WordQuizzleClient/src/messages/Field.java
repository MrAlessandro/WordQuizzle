package messages;

public class Field
{
    private short length;
    private char[] body;

    protected Field(char[] content)
    {
        this.body = content;
        this.length = (short) content.length;
    }

    public char[] getBody()
    {
        return this.body;
    }

    public short size()
    {
        return this.length;
    }

}
