import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class WordQuizzleClient
{
    public static void main(String[] args) throws IOException {
        String usr;
        Registrable remoteNet;

        WelcomeFrame gui = new WelcomeFrame();


       /* try
        {
            Registry r = LocateRegistry.getRegistry();
            remoteNet = (Registrable) r.lookup("WordQuizzleServer");

            System.out.println("New Username: ");
            usr = scanner.nextLine();
            while(!usr.equals(""))
            {
                remoteNet.registerUser(usr, "1234");

                System.out.println("New Username: ");
                usr = scanner.nextLine();
            }


        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }*/


    }
}
