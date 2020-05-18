package commons.messages;

import java.util.HashMap;

/**
 * Enumeration that itemize all possible message types.
 * Each type consists in a code (represented by a {@code short}) and the relative type name
 * (represented by a {@code String}).
 * <p>
 * In order to maintain an internal codes organization, codes are divided in three groups:
 * <li><b>Request message types</b>: which are dedicated for those messages sent by the client to the server which express the request of an operation</li>
 * <li><b>Notification message types</b>: which are dedicated for those messages asynchronously sent by the server to the client which express the notification of an event (Sent through UDP protocol)</li>
 * <li><b>Response message types</b>: which are dedicated for those messages sent by the server to the client which express the outcome of a requested operation</li>
 * @author Alessandro Meschi
 * @version 1.0
 */
public enum MessageType
{
    // Request messages
    LOG_IN((short) 1, "LogIn"),
    REQUEST_FOR_FRIENDSHIP((short) 2, "RequestForFriendship"),
    CONFIRM_FRIENDSHIP_REQUEST((short) 3, "ConfirmFriendshipRequest"),
    DECLINE_FRIENDSHIP_REQUEST((short) 4, "DeclineFriendshipRequest"),
    REQUEST_FOR_FRIENDS_LIST((short) 5, "RequestForFriendsList"),
    REQUEST_FOR_CHALLENGE((short) 6, "RequestForChallenge"),
    CONFIRM_CHALLENGE_REQUEST((short) 7, "ConfirmChallengeRequest"),
    DECLINE_CHALLENGE_REQUEST((short) 8, "DeclineChallengeRequest"),
    CHALLENGE_GET_WORD((short) 9, "ChallengeGetWord"),
    CHALLENGE_PROVIDE_TRANSLATION((short) 10, "ChallengeProvideTranslation"),
    REQUEST_FOR_SCORE_AMOUNT((short) 11, "RequestForScoreAmount"),
    REQUEST_FOR_FRIENDS_LIST_WITH_SCORES((short) 12, "RequestForFriendsListWithScores"),
    LOG_OUT((short) 13, "LogOut"),

    // Notifications,
    REQUEST_FOR_FRIENDSHIP_CONFIRMATION((short) 100, "RequestForFriendshipConfirmation"),
    FRIENDSHIP_REQUEST_CONFIRMED((short) 101, "FriendshipRequestConfirmed"),
    FRIENDSHIP_REQUEST_DECLINED((short)102, "FriendshipRequestDeclined"),
    REQUEST_FOR_CHALLENGE_CONFIRMATION((short) 103, "RequestForChallengeConfirmation"),
    CHALLENGE_REQUEST_CONFIRMED((short) 104, "ChallengeRequestConfirmed"),
    CHALLENGE_REQUEST_DECLINED((short) 105, "ChallengeRequestDeclined"),
    CHALLENGE_REQUEST_EXPIRED_APPLICANT((short) 106, "ChallengeRequestExpiredApplicant"),
    CHALLENGE_REQUEST_EXPIRED_RECEIVER((short) 107, "ChallengeRequestExpiredReceiver"),
    CHALLENGE_REQUEST_OPPONENT_LOGGED_OUT((short)108, "ChallengeRequestOpponentLoggedOut"),
    CHALLENGE_EXPIRED((short)109, "ChallengeExpired"),
    CHALLENGE_REPORT((short)110,"ChallengeReport"),
    CHALLENGE_OPPONENT_LOGGED_OUT((short)111, "ChallengeOpponentLoggedOut"),
    FRIEND_SCORE_UPDATE((short)112, "FriendScoreUpdate"),

    // Response messages
    OK((short) 200, "Ok"),
    FRIENDS_LIST((short) 201, "YourFriendList"),
    USER_ALREADY_LOGGED((short) 202, "UserAlreadyLogged"),
    PASSWORD_WRONG((short) 203, "PasswordWrong"),
    USERNAME_UNKNOWN((short) 204, "UsernameUnknown"),
    UNKNOWN_RECEIVER((short) 205, "UnknownReceiver"),
    ALREADY_FRIENDS((short) 206, "AlreadyFriends"),
    FRIENDSHIP_REQUEST_ALREADY_SENT ((short) 207, "FriendshipRequestAlreadySent"),
    FRIENDSHIP_REQUEST_ALREADY_RECEIVED ((short) 208, "FriendshipRequestAlreadyReceived"),
    INVALID_MESSAGE_FORMAT((short) 209, "InvalidMessageFormat"),
    PREVIOUS_CHALLENGE_REQUEST_SENT((short) 210, "PreviousChallengeRequestSent"),
    PREVIOUS_CHALLENGE_REQUEST_RECEIVED((short) 211, "PreviousChallengeRequestToReply"),
    RECEIVER_ENGAGED_IN_OTHER_CHALLENGE_REQUEST((short) 212, "ReceiverEngagedInOtherChallengeRequest"),
    RECEIVER_OFFLINE((short) 213, "ReceiverOffline"),
    RECEIVER_ENGAGED_IN_OTHER_CHALLENGE((short) 214, "ReceiverEngagedInOtherChallenge"),
    UNEXPECTED_MESSAGE((short) 215, "UnexpectedMessage"),
    TRANSLATION_CORRECT((short) 216, "TranslationCorrect"),
    TRANSLATION_WRONG((short) 217, "TranslationWrong"),
    SCORE_AMOUNT((short)218, "ScoreAmount"),
    FRIENDS_LIST_WITH_SCORES((short)219, "FriendsListWithScores");


    /**
     * The map containing the associations between codes and names
     */
    private static final HashMap<Short, MessageType> map = new HashMap<>(50);
    private final short value;
    private final String name;

    static
    {
        for (MessageType type : MessageType.values())
        {
            map.put(type.value, type);
        }
    }

    MessageType(short value, String name)
    {
        this.value = value;
        this.name = name;
    }

    /**
     * Gets the {@code short} code relative to this {@link MessageType}
     * @return The {@code short} code relative to this {@link MessageType}
     */
    public short getCode()
    {
        return value;
    }

    /**
     * Check if this {@link MessageType} is a request message type
     * @return {@code true} if this {@link MessageType} is a request message type, {@code false} otherwise
     */
    public boolean isRequest()
    {
        return this.value >= 0 && this.value < 100;
    }

    /**
     * Check if this {@link MessageType} is a notification message type
     * @return {@code true} if this {@link MessageType} is a notification message type, {@code false} otherwise
     */
    public boolean isNotification()
    {
        return this.value >= 100 && this.value < 200;
    }

    /**
     * Check if this {@link MessageType} is a response message type
     * @return {@code true} if this {@link MessageType} is a response message type, {@code false} otherwise
     */
    public boolean isResponse()
    {
        return this.value >= 200 && this.value < 300;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    /**
     * Get the {@link MessageType} relative to the given code
     * @param code Code of which we want to know the relative {@link MessageType}
     * @return The {@link MessageType} relative to the given {@code code}
     */
    public static MessageType valueOf(short code)
    {
        return map.get(code);
    }
}


