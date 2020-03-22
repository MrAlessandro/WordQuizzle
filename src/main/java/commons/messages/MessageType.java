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
    /**
     * Login request message type
     */
    LOG_IN((short) 1, "LogIn"),
    REQUEST_FOR_FRIENDSHIP((short) 3, "RequestForFriendship"),
    CONFIRM_FRIENDSHIP_REQUEST((short) 8, "ConfirmFriendshipRequest"),
    DECLINE_FRIENDSHIP_REQUEST((short) 9, "DeclineFriendshipRequest"),
    REQUEST_FOR_FRIENDS_LIST((short) 4, "RequestForFriendsList"),
    REQUEST_FOR_CHALLENGE((short) 5, "RequestForChallenge"),
    CONFIRM_CHALLENGE_REQUEST((short) 6, "ConfirmChallengeRequest"),
    DECLINE_CHALLENGE_REQUEST((short) 7, "DeclineChallengeRequest"),
    CHALLENGE_GET_WORD((short) 12, "ChallengeGetWord"),
    CHALLENGE_PROVIDE_TRANSLATION((short)13, "ChallengeProvideTranslation"),

    /*LOG_OUT((short) 2, "LogOut"),
    REQUEST_FOR_FRIENDSHIP((short) 3, "RequestForFriend"),
    REQUEST_FOR_FRIENDS_LIST((short) 4, "FriendList"),
    REQUEST_FOR_CHALLENGE((short) 5, "RequestForChallenge"),
    CONFIRM_CHALLENGE((short) 6, "ConfirmChallenge"),
    DECLINE_CHALLENGE((short) 7, "DeclineChallenge"),
    CONFIRM_FRIENDSHIP((short) 8, "ConfirmFriendship"),
    DECLINE_FRIENDSHIP((short) 9, "DeclineFriendship"),
    PROVIDE_TRANSLATION((short) 10, "NextWord"),
    REQUEST_FOR_FRIENDS_SCORES((short) 11, "FriendsScore"),*/

    // Notifications,
    REQUEST_FOR_FRIENDSHIP_CONFIRMATION((short) 100, "RequestForFriend"),
    FRIENDSHIP_REQUEST_CONFIRMED((short) 101, "FriendshipRequestConfirmed"),
    FRIENDSHIP_REQUEST_DECLINED((short)102, "FriendshipRequestDeclined"),
    CHALLENGE_REQUEST_CONFIRMED((short) 104, "ChallengeRequestConfirmed"),
    CHALLENGE_REQUEST_DECLINED((short) 105, "ChallengeRequestDeclined"),
    CHALLENGE_REQUEST_EXPIRED((short) 106, "ChallengeRequestExpired"),
    CHALLENGE_EXPIRED((short)110, "ChallengeExpired"),
    /*
    REQUEST_FOR_CHALLENGE_CONFIRMATION((short) 103, "NotifyChallenge"),
    CHALLENGE_CONFIRMED((short) 104, "ChallengeConfirmed"),
    CHALLENGE_REQUEST_TIMEOUT_EXPIRED((short) 106, "ChallengeRequestTimeoutExpired"),
    OPPONENT_WENT_OFFLINE_DURING_REQUEST((short) 107, "OpponentWentOfflineDuringRequest"),
    APPLICANT_WENT_OFFLINE_DURING_REQUEST((short) 108, "ApplicantWentOfflineDuringRequest"),
    OPPONENT_DID_NOT_REPLY((short) 109, "OpponentDidNotReply"),
    CHALLENGE_TIMEOUT_EXPIRED((short)110, "ChallengeTimeoutExpired"),
    CHALLENGE_REPORT((short) 111, "Challenge report"),
    OPPONENT_CANCELED_CHALLENGE((short)112, "OpponentCanceledChallenge"),
    APPLICANT_WENT_OFFLINE_DURING_CHALLENGE ((short)113, "ApplicantWentOfflineDuringChallenge"),
    OPPONENT_WENT_OFFLINE_DURING_CHALLENGE ((short)114, "OpponentWentOfflineDuringChallenge"),
    SCORE_UPDATE ((short) 115, "ScoreUpdate"),
    */

    // Response messages
    /*
    OK((short) 200, "Ok"),
    FRIENDS_LIST((short) 201, "YourFriendList"),
    ALREADY_FRIENDS((short) 203, "AlreadyFriends"),
    USERNAME_UNKNOWN((short) 204, "UsernameUnknown"),
    PASSWORD_WRONG((short) 205, "PasswordWrong"),
    INVALID_MESSAGE_FORMAT((short) 206, "InvalidMessageFormat"),
    UNEXPECTED_MESSAGE((short) 207, "UnexpectedMessage"),
    FRIENDSHIP_REQUEST_ALREADY_SENT ((short) 208, "FriendshipRequestAlreadySent"),
    OPPONENT_OFFLINE ((short) 209, "OpponentOffline"),
    OPPONENT_ALREADY_ENGAGED ((short) 210, "OpponentAlreadyEngaged"),
    APPLICANT_ALREADY_ENGAGED ((short) 221, "ApplicantAlreadyEngaged"),
    OPPONENT_NOT_FRIEND ((short) 212, "OpponentNotFriend"),
    TRANSLATION_WRONG((short) 213, "TranslationWrong"),
    FRIENDS_SCORES((short) 214, "FriendsScores"),
    USERNAME_ALREADY_USED((short) 215, "UsernameAlreadyUsed");*/
    OK((short) 200, "Ok"),
    FRIENDS_LIST((short) 201, "YourFriendList"),
    USER_ALREADY_LOGGED((short) 202, "UserAlreadyLogged"),
    PASSWORD_WRONG((short) 203, "PasswordWrong"),
    USERNAME_UNKNOWN((short) 204, "UsernameUnknown"),
    UNKNOWN_RECEIVER_EXCEPTION((short) 205, "UnknownReceiver"),
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
    TRANSLATION_WRONG((short) 217, "TranslationWrong");


    /**
     * The map containing the associations between codes and names
     */
    private static HashMap<Short, MessageType> map = new HashMap<>(50);
    private short value;
    private String name;

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
        return this.value >= 300 && this.value < 400;
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


