package commons.remote.exceptions;

/**
 * Signals the attempt of a registration process with an empty username within the server.users system
 * @author Alessandro Meschi
 * @version 1.0
 */
public class VoidUsernameException extends Exception
{
    /**
     * Initialize a new {@link VoidUsernameException}
     * @param message Message to be passed to the {@link Exception} constructor
     */
    public VoidUsernameException(String message)
    {
        super(message);
    }
}
