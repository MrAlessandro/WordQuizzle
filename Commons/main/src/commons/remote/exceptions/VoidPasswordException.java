package commons.remote.exceptions;

/**
 * Signals the attempt of a registration process with an empty password within the server.users system
 * @author Alessandro Meschi
 * @version 1.0
 */
public class VoidPasswordException extends Exception
{
    /**
     * Initialize a new {@link VoidPasswordException}
     * @param message Message to be passed to the {@link Exception} constructor
     */
    public VoidPasswordException(String message)
    {
        super(message);
    }
}
