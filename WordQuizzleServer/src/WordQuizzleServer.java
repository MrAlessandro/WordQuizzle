import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

class WordQuizzleServer
{
    private final static int CONNECTION_PORT = 50500;
    private final static String HOST_NAME = "localhost";

    public static void main(String[] args)
    {
        Registrable stub;
        Registry registry ;
        Selector selector = null;
        ServerSocketChannel connectionSocket = null;
        InetSocketAddress serverAddress = new InetSocketAddress(HOST_NAME, CONNECTION_PORT);

        // Restoring previous server state
        UserNet.restoreNet();

        // Enabling RMI support for registration operation
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
        }

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

                            AnsiColors.printGreen("ACCEPTED");
                        }
                        else
                            AnsiColors.printRed("REFUSED");
                    }

                    if (currentKey.isReadable())
                    {
                        SocketChannel client = (SocketChannel) currentKey.channel();
                        Session session = (Session) currentKey.attachment();
                        currentKey.interestOps(0);
                        TokenStack.add(new Token(client, session, currentKey, OperationType.READ));
                    }

                    if (currentKey.isWritable())
                    {
                        SocketChannel client = (SocketChannel) currentKey.channel();
                        Session session = (Session) currentKey.attachment();
                        currentKey.interestOps(0);
                        client.configureBlocking(true);

                        TokenStack.add(new Token(client, session, currentKey, OperationType.WRITE));
                    }
                }

                /*Token putBack = null;
                while ((putBack = TokenBackStack.get()) != null)
                {
                    if(putBack.OpType == OperationType.READ)
                        putBack.Key.interestOps(SelectionKey.OP_READ);
                    else if (putBack.OpType == OperationType.WRITE)
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

        UserNet.printNet();
    }
}