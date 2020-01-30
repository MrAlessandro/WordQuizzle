package server.main;

import server.dispatching.Delegation;
import server.dispatching.DelegationsDispenser;
import server.dispatching.OperationType;
import messages.Message;
import remote.Registrable;
import server.users.UsersManager;
import server.printer.AnsiColors;

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
    protected volatile static boolean STOP = false;

    public static void main(String[] args)
    {
        Registrable stub;
        Registry registry ;
        Selector selector;
        ServerSocketChannel connectionSocket;
        InetSocketAddress serverAddress = new InetSocketAddress(HOST_NAME, CONNECTION_PORT);

        // Registering a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutter()));

        Thread r = new Thread(new Executor());
        r.start();


        // Enabling RMI support for registration operation
        /*
        try
        {
            stub = (Registrable) UnicastRemoteObject.exportObject(UserNet.getNet(), 0);
            LocateRegistry.createRegistry(constants.Constants.UserNetRegistryPort);
            registry = LocateRegistry.getRegistry(constants.Constants.UserNetRegistryPort);
            registry.bind("server.main.WordQuizzleServer", stub);
        }
        catch (AlreadyBoundException | RemoteException e)
        {
            e.printStackTrace();
        }*/

        try
        {
            // Restoring previous server state
            UsersManager.restore();

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

                Delegation delegatedBack;
                while ((delegatedBack = DelegationsDispenser.getDelegationBack()) != null)
                {
                    if (delegatedBack.getType() == OperationType.CLOSE)
                    {
                        delegatedBack.getSelection().cancel();
                        delegatedBack.getSelection().channel().close();
                    }
                    else
                    {
                        int ops = SelectionKey.OP_READ;
                        if (delegatedBack.getSelection().attachment() instanceof Message || UsersManager.hasPendingMessages(delegatedBack.getSelection()))
                            ops = ops | SelectionKey.OP_WRITE;

                        delegatedBack.getSelection().interestOps(ops);
                    }
                }
            }

            selector.close();
            connectionSocket.close();

            UsersManager.backUp();
            UsersManager.print();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}