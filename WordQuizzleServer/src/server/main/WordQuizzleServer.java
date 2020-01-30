package server.main;

import server.dispatching.Delegation;
import server.dispatching.DelegationsDispenser;
import server.dispatching.OperationType;
import messages.Message;
import remote.Registrable;
import server.users.UsersManager;
import server.printer.AnsiColors;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.registry.Registry;
import java.util.Iterator;
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

            UsersManager.print();

            selector = Selector.open();
            connectionSocket = ServerSocketChannel.open();
            connectionSocket.bind(serverAddress);
            connectionSocket.configureBlocking(false);
            connectionSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (!STOP)
            {
                selector.select(10);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                while (keyIter.hasNext())
                {
                    SelectionKey currentKey = keyIter.next();
                    keyIter.remove();

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
                        DelegationsDispenser.delegateRead((SocketChannel) currentKey.channel(), currentKey.attachment());
                    }

                    if (currentKey.isWritable())
                    {
                        currentKey.interestOps(0);
                        DelegationsDispenser.delegateWrite((SocketChannel) currentKey.channel(), currentKey.attachment());
                    }
                }

                Delegation delegatedBack;
                while ((delegatedBack = DelegationsDispenser.getDelegationBack()) != null)
                {
                    if (delegatedBack.getType() == OperationType.CLOSE)
                    {
                        delegatedBack.getSelection().keyFor(selector).cancel();
                        delegatedBack.getSelection().close();
                    }
                    else
                    {
                        int ops = SelectionKey.OP_READ;
                        if (delegatedBack.attachment() instanceof Message)
                            ops = ops | SelectionKey.OP_WRITE;

                        delegatedBack.getSelection().keyFor(selector).attach(delegatedBack.attachment());
                        delegatedBack.getSelection().keyFor(selector).interestOps(ops);
                    }
                }

                for (SelectionKey key : selector.keys())
                {
                    if (key.attachment() instanceof String && UsersManager.hasPendingMessages((String) key.attachment()))
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }

                /*Set<SelectionKey> writables = UsersManager.getWritables();
                for (SelectionKey key : writables)
                {
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }*/
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