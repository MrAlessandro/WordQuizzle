package server;

import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.settings.ServerConstants;
import server.users.UsersManager;
import server.users.exceptions.WrongPasswordException;
import server.users.user.User;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class TestUsersManager
{
    private ConcurrentHashMap<String, User> usersArchive;

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
    public void setUpUsersManager()
    {
        UsersManager.setUp();

        try
        {
            Field field = UsersManager.class.getDeclaredField("usersArchive");
            field.setAccessible(true);
            usersArchive = (ConcurrentHashMap<String, User>) field.get(null);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            fail("ERROR GETTING USERS ARCHIVE PRIVATE FIELD");
        }
    }

    @Test
    public void testRegistration()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();

        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
        assertNotNull(UsersManager.getUser(username));
        assertEquals(1, usersArchive.size());
    }

    @Test
    public void testRegistrationUsernameUsed()
    {
        String commonUsername = UUID.randomUUID().toString();
        char[] password1 = UUID.randomUUID().toString().toCharArray();
        char[] password2 = UUID.randomUUID().toString().toCharArray();

        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(commonUsername, password1));
        assertThrows(UsernameAlreadyUsedException.class, () -> UsersManager.getUsersManager().registerUser(commonUsername, password2));
        assertEquals(1, usersArchive.size());
    }

    @Test
    public void testRegistrationVoidUsername()
    {
        char[] password = UUID.randomUUID().toString().toCharArray();
        assertThrows(VoidUsernameException.class, () -> UsersManager.getUsersManager().registerUser("", password));
        assertEquals(0, usersArchive.size());
    }

    @Test
    public void testRegistrationVoidPassword()
    {
        char[] password = {};
        assertThrows(VoidPasswordException.class, () -> UsersManager.getUsersManager().registerUser(UUID.randomUUID().toString(), password));
        assertEquals(0, usersArchive.size());
    }

    @Test
    public void testPasswordCheckingCorrect()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);

        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
        assertNotNull(UsersManager.getUser(username));
        assertEquals(1, usersArchive.size());
        assertDoesNotThrow(() -> UsersManager.getUser(username).checkPassword(passwordCopy));
    }

    @Test
    public void testPasswordCheckingWrong()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        passwordCopy[0] = (char) (password[0] + 1);

        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
        assertNotNull(UsersManager.getUser(username));
        assertEquals(1, usersArchive.size());
        assertThrows(WrongPasswordException.class, () -> UsersManager.getUser(username).checkPassword(passwordCopy));
    }

    @Test
    public void testFriendship()
    {
        String username1 = UUID.randomUUID().toString();
        char[] password1 = UUID.randomUUID().toString().toCharArray();
        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username1, password1));

        String username2 = UUID.randomUUID().toString();
        char[] password2 = UUID.randomUUID().toString().toCharArray();
        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username2, password2));

        User user1 = UsersManager.getUser(username1);
        User user2 = UsersManager.getUser(username2);

        assertNotNull(user1);
        assertNotNull(user2);

        assertEquals(2, usersArchive.size());

        UsersManager.makeFriends(user1, user2);

        assertTrue(UsersManager.areFriends(user1, user2));
    }

    @Nested
    class TestUsersManagerConcurrently
    {
        private ExecutorService pool;

        @BeforeEach
        public void setUp()
        {
            this.pool = Executors.newFixedThreadPool(ServerConstants.DEPUTIES_POOL_SIZE);
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentRegistration(int tasksNum)
        {
            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String username = UUID.randomUUID().toString();
                    char[] password = UUID.randomUUID().toString().toCharArray();

                    assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
                    assertNotNull(UsersManager.getUser(username));
                });
            }

            pool.shutdownNow();
            assertDoesNotThrow(() -> pool.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(tasksNum, usersArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentPasswordCheckingCorrect(int tasksNum)
        {
            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String username = UUID.randomUUID().toString();
                    char[] password = UUID.randomUUID().toString().toCharArray();
                    char[] passwordCopy = Arrays.copyOf(password, password.length);

                    assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
                    assertNotNull(UsersManager.getUser(username));
                    assertDoesNotThrow(() -> UsersManager.getUser(username).checkPassword(passwordCopy));
                });
            }

            pool.shutdownNow();
            assertDoesNotThrow(() -> pool.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(tasksNum, usersArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentPasswordCheckingWrong(int tasksNum)
        {
            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String username = UUID.randomUUID().toString();
                    char[] password = UUID.randomUUID().toString().toCharArray();
                    char[] passwordCopy = Arrays.copyOf(password, password.length);
                    passwordCopy[0] = (char) (password[0] + 1);

                    assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
                    assertNotNull(UsersManager.getUser(username));
                    assertThrows(WrongPasswordException.class, () -> UsersManager.getUser(username).checkPassword(passwordCopy));
                });
            }

            pool.shutdownNow();
            assertDoesNotThrow(() -> pool.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(tasksNum, usersArchive.size());
        }
    }
}
