package client.operators;

import client.main.WordQuizzleClient;

import javax.swing.*;

public class FriendshipRequestOperator extends SwingWorker <Boolean, Void>
{
    private String from;
    private String to;

    public FriendshipRequestOperator(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        int response = JOptionPane.showConfirmDialog(null, "User \"" + this.from + "\" sent you a friendship request \n Do you want to accept it?", "Friendship request", JOptionPane.YES_NO_OPTION);
        if (response == 0)
            // Yes
            WordQuizzleClient.confirmFriendshipRequest(this.from, this.to);
        else
            // No
            WordQuizzleClient.declineFriendshipRequest(this.from, this.to);

        return true;
    }
}
