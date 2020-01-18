import dispatching.Delegation;
import dispatching.DelegationsDispenser;
import dispatching.OperationType;
import users.Registrable;
import users.UsersManager;
import util.AnsiColors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.registry.Registry;
import java.util.Set;

class WordQuizzleServer
{
    private final static int CONNECTION_PORT = 50500;
    private final static String HOST_NAME = "localhost";

    public static void main(String[] args) throws IOException
    {
        Registrable stub;
        Registry registry ;
        Selector selector = null;
        ServerSocketChannel connectionSocket = null;
        InetSocketAddress serverAddress = new InetSocketAddress(HOST_NAME, CONNECTION_PORT);

        // Restoring previous server state
        UsersManager.restoreNet();

        // Enabling RMI support for registration operation
        /*
        try
        {
            stub = (Registrable) UnicastRemoteObject.exportObject(UserNet.getNet(), 0);
            LocateRegistry.createRegistry(Constants.UserNetRegistryPort);
            registry = LocateRegistry.getRegistry(Constants.UserNetRegistryPort);
            registry.bind("WordQuizzleServer", stub);
        }
        catch (AlreadyBoundException | RemoteException e)
        {
            e.printStackTrace();
        }*/


        Thread t = new Thread(new Executor());
        t.start();

        try
        {
            selector = Selector.open();
            connectionSocket = ServerSocketChannel.open();
            connectionSocket.bind(serverAddress);
            connectionSocket.configureBlocking(false);
            connectionSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (true)
            {
                selector.select(10);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey currentKey : selectedKeys)
                {
                    selectedKeys.remove(currentKey);

                    if (currentKey.isAcceptable())
                    {
                        System.out.print("Accepting connection from a client... ");


                        ServerSocketChannel server = (ServerSocketChannel) currentKey.channel();
                        SocketChannel client = server.accept();

                        if (client != null)
                        {
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ).attach(null);

                            AnsiColors.printlnGreen("ACCEPTED");
                        }
                        else
                            AnsiColors.printlnRed("REFUSED");
                    }

                    if (currentKey.isReadable())
                    {
                        currentKey.interestOps(0);
                        DelegationsDispenser.add(new Delegation(currentKey, OperationType.READ));
                    }

                    if (currentKey.isWritable())
                    {
                        currentKey.interestOps(0);
                        DelegationsDispenser.add(new Delegation(currentKey, OperationType.WRITE));
                    }
                }

                /*
                CommunicationDispatching.Token putBack = null;
                while ((putBack = CommunicationDispatching.TokenBackStack.get()) != null)
                {
                    if(putBack.OpType == CommunicationDispatching.OperationType.READ)
                        putBack.Key.interestOps(SelectionKey.OP_READ);
                    else if (putBack.OpType == CommunicationDispatching.OperationType.WRITE)
                        putBack.Key.interestOps(SelectionKey.OP_WRITE);
                }*/

            }

            //selector.close();
            //connectionSocket.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        UsersManager.printNet();

    }
}