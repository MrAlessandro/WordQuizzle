package server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import server.sessions.SessionsManager;
import server.settings.ServerConstants;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class TestSessionsManager
{
    @BeforeAll
    public static void setUpProperties()
    {
        try
        {
            ServerConstants.loadProperties();
        }
        catch (IOException e)
        {
            fail("ERROR LOADING PROPERTIES");
        }
    }

    @BeforeEach
    public void setUpSessionsManager()
    {
        SessionsManager.setUp();
    }
}
