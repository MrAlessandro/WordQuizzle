package messages;


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

    public short size()
    {
        return this.length;
    }

}
