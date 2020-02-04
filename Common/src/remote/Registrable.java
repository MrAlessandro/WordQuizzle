package remote;

import java.rmi.Remote;

public interface Registrable extends Remote
{
    boolean registerUser(String userName, char[] password) throws VoidPasswordException, VoidUsernameException;
}
