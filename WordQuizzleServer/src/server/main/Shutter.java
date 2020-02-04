package server.main;

public class Shutter implements Runnable
{
    public void run()
    {
        WordQuizzleServer.shutDown();
    }
}
