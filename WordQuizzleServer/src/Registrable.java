import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registrable extends Remote
{
    boolean registerUser(String userName, String passwd) throws RemoteException;
}
