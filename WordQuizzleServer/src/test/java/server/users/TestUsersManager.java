package server.users;

import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.settings.Settings;
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
    private UsersManager usersManager;
    private ConcurrentHashMap<String, User> usersArchive;

    @BeforeAll
    public static void setUpProperties()
    {
        try
        {
            Settings.loadProperties();
        }
        catch (IOException e)
        {
            fail("ERROR LOADING PROPERTIES");
        }
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUpUsersManager()
    {
        try
        {
            this.usersManager = new UsersManager();
            Field field = UsersManager.class.getDeclaredField("usersArchive");
            field.setAccessible(true);
            usersArchive = (ConcurrentHashMap<String, User>) field.get(this.usersManager);
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

        assertDoesNotThrow(() -> this.usersManager.registerUser(username, password));
        assertNotNull(this.usersManager.getUser(username));
        assertEquals(1, usersArchive.size());
    }

    @Test
    public void testRegistrationUsernameUsed()
    {
        String commonUsername = UUID.randomUUID().toString();
        char[] password1 = UUID.randomUUID().toString().toCharArray();
        char[] password2 = UUID.randomUUID().toString().toCharArray();

        assertDoesNotThrow(() -> this.usersManager.registerUser(commonUsername, password1));
        assertThrows(UsernameAlreadyUsedException.class, () -> this.usersManager.registerUser(commonUsername, password2));
        assertEquals(1, usersArchive.size());
    }

    @Test
    public void testRegistrationVoidUsername()
    {
        char[] password = UUID.randomUUID().toString().toCharArray();
        assertThrows(VoidUsernameException.class, () -> this.usersManager.registerUser("", password));
        assertEquals(0, usersArchive.size());
    }

    @Test
    public void testRegistrationVoidPassword()
    {
        char[] password = {};
        assertThrows(VoidPasswordException.class, () -> this.usersManager.registerUser(UUID.randomUUID().toString(), password));
        assertEquals(0, usersArchive.size());
    }

    @Test
    public void testPasswordCheckingCorrect()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);

        assertDoesNotThrow(() -> this.usersManager.registerUser(username, password));
        assertNotNull(this.usersManager.getUser(username));
        assertEquals(1, usersArchive.size());
        assertDoesNotThrow(() -> this.usersManager.getUser(username).checkPassword(passwordCopy));
    }

    @Test
    public void testPasswordCheckingWrong()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        passwordCopy[0] = (char) (password[0] + 1);

        assertDoesNotThrow(() -> this.usersManager.registerUser(username, password));
        assertNotNull(this.usersManager.getUser(username));
        assertEquals(1, usersArchive.size());
        assertThrows(WrongPasswordException.class, () -> this.usersManager.getUser(username).checkPassword(passwordCopy));
    }

    @Test
    public void testFriendship()
    {
        String username1 = UUID.randomUUID().toString();
        char[] password1 = UUID.randomUUID().toString().toCharArray();
        assertDoesNotThrow(() -> this.usersManager.registerUser(username1, password1));

        String username2 = UUID.randomUUID().toString();
        char[] password2 = UUID.randomUUID().toString().toCharArray();
        assertDoesNotThrow(() -> this.usersManager.registerUser(username2, password2));

        User user1 = this.usersManager.getUser(username1);
        User user2 = this.usersManager.getUser(username2);

        assertNotNull(user1);
        assertNotNull(user2);

        assertEquals(2, usersArchive.size());

        this.usersManager.makeFriends(user1, user2);

        assertTrue(this.usersManager.areFriends(user1, user2));
    }

    @Nested
    class TestUsersManagerConcurrently
    {
        private ExecutorService pool;

        @BeforeEach
        public void setUp()
        {
            this.pool = Executors.newFixedThreadPool(Settings.DEPUTIES_POOL_SIZE);
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

                    assertDoesNotThrow(() -> usersManager.registerUser(username, password));
                    assertNotNull(usersManager.getUser(username));
                });
            }

            pool.shutdown();
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

                    assertDoesNotThrow(() -> usersManager.registerUser(username, password));
                    assertNotNull(usersManager.getUser(username));
                    assertDoesNotThrow(() -> usersManager.getUser(username).checkPassword(passwordCopy));
                });
            }

            pool.shutdown();
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

                    assertDoesNotThrow(() -> usersManager.registerUser(username, password));
                    assertNotNull(usersManager.getUser(username));
                    assertThrows(WrongPasswordException.class, () -> usersManager.getUser(username).checkPassword(passwordCopy));
                });
            }

            pool.shutdown();
            assertDoesNotThrow(() -> pool.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(tasksNum, usersArchive.size());
        }
    }
}
