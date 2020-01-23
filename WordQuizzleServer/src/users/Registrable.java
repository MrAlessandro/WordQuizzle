package users;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registrable extends Remote
{
    boolean registerUser(String userName, char[] password) throws RemoteException;
}
