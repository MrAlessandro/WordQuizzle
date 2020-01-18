import dispatching.DelegationsDispenser;
import exceptions.SessionsArchiveInconsistanceException;
import messages.Message;
import sessions.SessionsManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Writer implements Runnable
{
    public static final ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

    @Override
    public void run()
    {
        SelectionKey delegation = null;
        SocketChannel clientSocket = null;
        boolean stop = false;

        while (!stop)
        {
            try
            {
                // Gets the delegation from the inter-thread communication structure
                delegation = DelegationsDispenser.getWriteDelegation();
                // Extracts details from the delegation
                clientSocket = (SocketChannel) delegation.channel();
                if (delegation.attachment() instanceof Message)
                {// Error message for users which are not logged in
                    Message.writeMessage(clientSocket, buffer, (Message) delegation.attachment());
                }
                else if (delegation.attachment() instanceof String)
                {
                    Message toSend = SessionsManager.getPendingSessionMessage(clientSocket);
                    Message.writeMessage(clientSocket, buffer, toSend);
                }

                DelegationsDispenser.backDelegate(delegation);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (SessionsArchiveInconsistanceException e)
            {
                e.printStackTrace();
            }
        }
    }
}
