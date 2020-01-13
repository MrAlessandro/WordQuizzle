import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class WordQuizzleClient
{
    public static void main(String[] args)
    {

        WelcomeFrame gui = new WelcomeFrame();
        SwingUtilities.invokeLater(gui);
    }

    protected static boolean register(String username, char[] password)
    {
        boolean retValue = false;

        try
        {
            Registry r = LocateRegistry.getRegistry();
            Registrable remoteNet = (Registrable) r.lookup("WordQuizzleServer");
            retValue = remoteNet.registerUser(username, password);
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }

        return retValue;
    }
}
