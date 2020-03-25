package commons.remote.exceptions;

/**
 * Signals the attempt of a registration process using an username already used in the server.users system
 * @author Alessandro Meschi
 * @version 1.0
 */
public class UsernameAlreadyUsedException extends Exception
{
    /**
     * Initialize a new {@link UsernameAlreadyUsedException}
     * @param message Message to be passed to the {@link Exception} constructor
     */
    public UsernameAlreadyUsedException(String message)
    {
        super(message);
    }
}
