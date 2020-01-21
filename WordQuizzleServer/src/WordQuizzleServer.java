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
    public  static boolean STOP = false;

    public static void main(String[] args) throws IOException
    {
        Registrable stub;
        Registry registry ;
        Selector selector = null;
        ServerSocketChannel connectionSocket = null;
        InetSocketAddress serverAddress = new InetSocketAddress(HOST_NAME, CONNECTION_PORT);

        // Registering a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutter()));

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


        Thread r = new Thread(new Executor());
        r.start();

        try
        {
            selector = Selector.open();
            connectionSocket = ServerSocketChannel.open();
            connectionSocket.bind(serverAddress);
            connectionSocket.configureBlocking(false);
            connectionSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (!STOP)
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
                        DelegationsDispenser.delegateRead(currentKey);
                    }

                    if (currentKey.isWritable())
                    {
                        currentKey.interestOps(0);
                        DelegationsDispenser.delegateWrite(currentKey);
                    }
                }


                Delegation delegatedBack = null;
                while ((delegatedBack = DelegationsDispenser.getDelegationBack()) != null)
                {
                    switch (delegatedBack.getType())
                    {
                        case READ:
                            delegatedBack.getDelegation().interestOps(SelectionKey.OP_READ);
                            break;
                        case WRITE:
                            delegatedBack.getDelegation().interestOps(SelectionKey.OP_WRITE);
                            break;
                        case CLOSE:
                            delegatedBack.getDelegation().cancel();
                            delegatedBack.getDelegation().channel().close();
                            break;
                    }
                }

            }

            selector.close();
            connectionSocket.close();

        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        UsersManager.printNet();

    }
}