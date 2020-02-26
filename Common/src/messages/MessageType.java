package messages;

import java.util.HashMap;

public enum MessageType
{
    // Request messages
    LOG_IN((short) 1, "LogIn"),
    LOG_OUT((short) 2, "LogOut"),
    REQUEST_FOR_FRIENDSHIP((short) 3, "RequestForFriend"),
    REQUEST_FOR_FRIENDS_LIST((short) 4, "FriendList"),
    REQUEST_FOR_CHALLENGE((short) 5, "RequestForChallenge"),
    CONFIRM_CHALLENGE((short) 6, "ConfirmChallenge"),
    DECLINE_CHALLENGE((short) 7, "DeclineChallenge"),
    CONFIRM_FRIENDSHIP((short) 8, "ConfirmFriendship"),
    DECLINE_FRIENDSHIP((short) 9, "DeclineFriendship"),
    PROVIDE_TRANSLATION((short) 10, "NextWord"),
    REQUEST_FOR_FRIENDS_SCORES((short) 11, "FriendsScore"),

    // Notifications,
    REQUEST_FOR_FRIENDSHIP_CONFIRMATION((short) 100, "RequestForFriend"),
    FRIENDSHIP_CONFIRMED((short) 101, "FriendshipConfirmed"),
    FRIENDSHIP_DECLINED((short)102, "FriendshipDeclined"),
    REQUEST_FOR_CHALLENGE_CONFIRMATION((short) 103, "NotifyChallenge"),
    CHALLENGE_CONFIRMED((short) 104, "ChallengeConfirmed"),
    CHALLENGE_DECLINED((short) 105, "ChallengeDeclined"),
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

    // Response messages
    OK((short) 200, "Ok"),
    FRIENDS_LIST((short) 201, "YourFriendList"),
    ALREADY_FRIENDS((short) 203, "AlreadyFriends"),
    USERNAME_UNKNOWN((short) 204, "UsernameUnknown"),
    PASSWORD_WRONG((short) 205, "PasswordWrong"),
    INVALID_MESSAGE_FORMAT((short) 206, "InvalidMessage"),
    UNEXPECTED_MESSAGE((short) 207, "UnexpectedMessage"),
    FRIENDSHIP_REQUEST_ALREADY_SENT ((short) 208, "FriendshipRequestAlreadySent"),
    OPPONENT_OFFLINE ((short) 209, "OpponentOffline"),
    OPPONENT_ALREADY_ENGAGED ((short) 210, "OpponentAlreadyEngaged"),
    APPLICANT_ALREADY_ENGAGED ((short) 221, "ApplicantAlreadyEngaged"),
    OPPONENT_NOT_FRIEND ((short) 212, "OpponentNotFriend"),
    TRANSLATION_WRONG((short) 213, "TranslationWrong"),
    FRIENDS_SCORES((short) 214, "FriendsScores");



    private short value;
    private String name;
    private static HashMap Map = new HashMap<>();

    static
    {
        for (MessageType type : MessageType.values())
        {
            Map.put(type.value, type);
        }
    }

    MessageType(short value, String name)
    {
        this.value = value;
        this.name = name;
    }

    public short getValue()
    {
        return value;
    }

    public String toString()
    {
        return this.name;
    }

    public static MessageType valueOf(short type)
    {
        return (MessageType) Map.get(type);
    }

    public boolean isRequest()
    {
        return this.value >= 0 && this.value < 100;
    }

    public boolean isNotification()
    {
        return this.value >= 100 && this.value < 200;
    }

    public boolean isResponse(MessageType type)
    {
        return this.value >= 300 && this.value < 400;
    }
}


