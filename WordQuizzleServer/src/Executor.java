import dispatching.Delegation;
import dispatching.DelegationsDispenser;
import dispatching.OperationType;
import returns.ReturnValue;
import status.Status;
import messages.Message;
import messages.MessageType;
import users.UsersManager;
import util.AnsiColors;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Executor implements Runnable
{
    private static final ByteBuffer buffer = ByteBuffer.allocate(2048);

    @Override
    public void run()
    {
        SocketChannel clientSocket;
        Delegation delegation;
        Object attachment;
        OperationType opType;
        boolean stop = false;

        while (!stop)
        {
            // Gets the delegation from the inter-thread communication structure
            delegation = DelegationsDispenser.getDelegation();
            // Extracts details from the delegation
            clientSocket = delegation.getChannel();
            attachment = delegation.getAttachment();
            opType = delegation.getType();

            switch (opType)
            {
                case READ:
                {
                    // Read message
                    Message message = Message.readMessage(clientSocket, buffer);
                    if (message == null)
                    {// Close connection on empty read
                        if (attachment instanceof String)
                            UsersManager.closeSession((String) attachment);
                        delegation.setType(OperationType.CLOSE);
                        DelegationsDispenser.delegateBack(delegation);
                        break;
                    }

                    switch (message.getType())
                    {
                        // LogIn operation
                        case LOG_IN:
                        {
                            String username = String.valueOf(message.getField(0));
                            char[] password = message.getField(1);
                            ReturnValue returnValue;

                            System.out.print("Logging in user \"" + username + "\"... ");

                            returnValue = UsersManager.openSession(username, password, delegation.getChannel());

                            switch ((Status) returnValue.status)
                            {
                                case USER_UNKNOWN:
                                case WRONG_PASSWORD:
                                    AnsiColors.printlnRed(returnValue.status.toString());
                                    delegation.attach(new Message(MessageType.valueOf(((Status) returnValue.status).getValue())));
                                    delegation.setType(OperationType.WRITE_TO_UNLOGGED);
                                    break;
                                case SUCCESS:
                                    AnsiColors.printlnGreen(returnValue.status.toString());
                                    delegation.attach(username);
                                    delegation.setType(OperationType.UNDEFINED);
                                    break;
                            }

                            // Reinsert socket in the communication dispatching
                            DelegationsDispenser.delegateBack(delegation);
                            break;
                        }
                        case ADD_FRIEND:
                        {
                            /*Message confirmMessage;
                            String friend = String.valueOf(message.getField(0));
                            System.out.println("Sending a friendship request from \"" + username + "\" to \"" + friend + "\"... ");

                            confirmMessage = new Message(MessageType.CONFIRM_FRIENDSHIP, username);

                            AnsiColors.printlnGreen("SENT");

                            UsersManager.makeFriends(username, friend);*/


                        }
                        default:
                        {

                        }
                    }

                    break;
                }
                case WRITE:
                {
                    String username = (String) delegation.getAttachment();
                    ReturnValue returnValue = UsersManager.retrieveMessage(username);
                    if (returnValue.status == Status.USER_UNKNOWN)
                        throw new RuntimeException("System inconsistency");

                    Message.writeMessage(clientSocket, buffer, (Message) returnValue.value);

                    delegation.setType(OperationType.UNDEFINED);
                    DelegationsDispenser.delegateBack(delegation);

                    break;
                }
                case WRITE_TO_UNLOGGED:
                {
                    Message message = (Message) delegation.getAttachment();
                    Message.writeMessage(clientSocket, buffer, message);
                    delegation.attach(null);

                    delegation.setType(OperationType.READ);
                    DelegationsDispenser.delegateBack(delegation);

                    break;
                }
            }

        }
    }
}

