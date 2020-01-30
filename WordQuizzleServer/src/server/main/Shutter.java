package server.main;

public class Shutter implements Runnable
{
    public void run()
    {
        WordQuizzleServer.STOP = true;
        System.out.println("Server is shutting down");
    }
}
