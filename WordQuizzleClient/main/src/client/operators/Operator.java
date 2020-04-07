package client.operators;

import client.gui.WordQuizzleClientFrame;

import javax.swing.*;

abstract public class Operator extends SwingWorker<Void, Void>
{
    protected WordQuizzleClientFrame frame;

    public Operator(WordQuizzleClientFrame frame)
    {
        this.frame = frame;
    }
}
