package server.requests.friendship;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.requests.friendship.FriendshipRequestsManager;
import server.requests.friendship.exceptions.FriendshipRequestAlreadyReceived;
import server.requests.friendship.exceptions.FriendshipRequestAlreadySent;
import server.settings.ServerConstants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestFriendshipRequestsManager
{
    private ConcurrentHashMap<String, Set<String>> friendshipRequestsArchive;

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
    public void setUpFriendshipRequestsManager()
    {
        FriendshipRequestsManager.setUp();

        try
        {
            Field field = FriendshipRequestsManager.class.getDeclaredField("friendshipRequestsArchive");
            field.setAccessible(true);
            friendshipRequestsArchive = (ConcurrentHashMap<String, Set<String>>) field.get(null);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            fail("ERROR GETTING FRIENDSHIP REQUESTS ARCHIVE PRIVATE FIELD");
        }
    }

    @Test
    public void testFriendshipRequestRecording()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(username1, username2));
        assertEquals(1, friendshipRequestsArchive.size());
    }

    @Test
    public void testFriendshipRequestAlreadySent()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(username1, username2));
        assertThrows(FriendshipRequestAlreadySent.class, () -> FriendshipRequestsManager.recordFriendshipRequest(username1, username2));
        assertEquals(1, friendshipRequestsArchive.size());
    }

    @Test
    public void testFriendshipRequestAlreadyReceived()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(username1, username2));
        assertThrows(FriendshipRequestAlreadyReceived.class, () -> FriendshipRequestsManager.recordFriendshipRequest(username2, username1));
        assertEquals(1, friendshipRequestsArchive.size());
    }

    @Test
    public void testFriendshipRequestsDiscarding()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(username1, username2));
        assertTrue(FriendshipRequestsManager.discardFriendshipRequest(username1, username2));
        assertEquals(0, friendshipRequestsArchive.size());

        // Try again in reverse order
        assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(username2, username1));
        assertTrue(FriendshipRequestsManager.discardFriendshipRequest(username2, username1));
        assertEquals(0, friendshipRequestsArchive.size());
    }

    @Nested
    class TestFriendshipRequestsManagerConcurrently
    {
        private ExecutorService pool;

        @BeforeEach
        public void setUp()
        {
            this.pool = Executors.newFixedThreadPool(ServerConstants.DEPUTIES_POOL_SIZE);
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentFriendshipRequestRecording(int tasksNum)
        {
            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String username1 = UUID.randomUUID().toString();
                    String username2 = UUID.randomUUID().toString();

                    assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(username1, username2));
                });
            }

            pool.shutdown();
            assertDoesNotThrow(() -> pool.awaitTermination(tasksNum, TimeUnit.SECONDS));
            assertEquals(tasksNum, friendshipRequestsArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentFriendshipRequestRecordingOneToMany(int tasksNum)
        {
            String one = UUID.randomUUID().toString();

            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String other = UUID.randomUUID().toString();

                    assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(one, other));
                });
            }

            pool.shutdownNow();
            assertDoesNotThrow(() -> pool.awaitTermination(tasksNum, TimeUnit.SECONDS));
            assertEquals(tasksNum, friendshipRequestsArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentFriendshipRequestRecordingManyToOne(int tasksNum)
        {
            String one = UUID.randomUUID().toString();

            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String other = UUID.randomUUID().toString();

                    assertDoesNotThrow(() -> FriendshipRequestsManager.recordFriendshipRequest(other, one));
                });
            }

            pool.shutdownNow();
            assertDoesNotThrow(() -> pool.awaitTermination(tasksNum, TimeUnit.SECONDS));
            assertEquals(1, friendshipRequestsArchive.size());
        }
    }
}
