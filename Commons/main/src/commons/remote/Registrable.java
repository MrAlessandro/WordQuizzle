package commons.remote;

import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represent the interface used by the RMI support to expose the remotely callable method
 * @author Alessandro Meschi
 * @version 1.0
 */
public interface Registrable extends Remote
{
    /**
     * Attempt to register a new User with given {@code username} and {@code password} putting it in the server's server.users
     * database.
     * @param username The username of the user which is supposed to be registered
     * @param password The password of the user which is supposed to be registered
     * @throws VoidPasswordException If the given {@code password} is null or an empty string
     * @throws VoidUsernameException If the given {@code username} is null or an empty string
     * @throws UsernameAlreadyUsedException If an user with the given {@code username} is already registered in the
     * database
     * @throws RemoteException If occur an error during the RMI session.
     */
    void registerUser(String username, char[] password) throws VoidPasswordException, VoidUsernameException, UsernameAlreadyUsedException, RemoteException;
}
