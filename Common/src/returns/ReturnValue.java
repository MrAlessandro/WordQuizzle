package returns;

public class ReturnValue
{
    public Enum status;
    public Object value;

    public ReturnValue(Enum status, Object value)
    {
        this.status = status;
        this.value = value;
    }
}
