import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class WordQuizzleServer
{
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {

        String passwd = "1234";

        Registrable stub = (Registrable) UnicastRemoteObject.exportObject(UserNet.getNet(), 0);

        LocateRegistry.createRegistry(Constants.UserNetRegistryPort);

        Registry r = LocateRegistry.getRegistry(Constants.UserNetRegistryPort);

        r.bind("WordQuizzleServer", stub);

    /*
           Net.registerUser("Fabio", passwd);
           Net.registerUser("Lorenzo", passwd);
           Net.registerUser("Martina", passwd);
           Net.registerUser("Claudia", passwd);
           Net.registerUser("Andrea", passwd);

           Net.addFriendship("Fabio", "Lorenzo");
           Net.addFriendship("Lorenzo", "Martina");
           Net.addFriendship("Claudia", "Andrea");
           Net.addFriendship("Andrea", "Fabio");
           Net.addFriendship("Fabio", "Claudia");

           Net.backUpNet();
    */

        UserNet.restoreNet();

        UserNet.printNet();

    }
}