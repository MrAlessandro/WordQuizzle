package users.exceptions;

public class AlreadyExistingRelationshipException extends Exception
{
    public AlreadyExistingRelationshipException(String message)
    {
        super(message);
    }
}
