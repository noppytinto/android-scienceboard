package com.nocorp.scienceboard.utility;

public class MyValues {

    public enum ItemType {
        ARTICLE,
        LIST_AD,
        VISITED_ARTICLE,
        BOOKMARKED_ARTICLE,
        PROGRESS_INDICATOR
    }

    public enum SourceCategory {
        SCIENCE,
        TECH,
        SPACE,
        NATURE,
        MEDICINE,
        BIOLOGY,
        PHYSICS
    }


    public enum DownloadStatus {
        IDLE,
        PROCESSING,
        NOT_INITILIZED,
        FAILED,
        SUCCESS,
        NO_RESULT,
        NONE
    }

    public enum FetchStatus {
        PROCESSING,
        NOT_INITILIZED,
        FAILED,
        NOT_EXISTS,
        SUCCESS,
        MOVIES_DETAILS_DOWNLOADED,
        REFETCH,
        EMPTY,
        FOLLOWERS_FETCHED,
        FOLLOWING_FETCHED,
        FOLLOWING_FETCH_FAILED,
        FOLLOWERS_FETCH_FAILED,
        NO_FOLLOWERS,
        NO_FOLLOWING,
        CACHED,
        IDLE
    }

    public enum TaskStatus {
        COMMENT_DELETED,
        COMMENT_NOT_DELETED,
        COMMENT_POSTED,
        COMMNET_NOT_POSTED,
        SUCCESS,
        FAILED,
        SUBSCRIBED,
        UNSUBSCRIBED,
        FAILED_SUBSCRIPTION,
        FAILED_UNSUBSCRIPTION,
        ALREADY_SUBSCRIBED,
        NOT_SUBSCRIBED,
        SUBSCRIPTION_CHECK_FAILED,
        COMMENT_ADDED,
        COMMENT_NOT_ADDED,
        COMMENT_FAILED,
        LIKE_ADDED,
        LIKE_NOT_ADDED,
        LIKE_ADDED_FAIL,
        LIKE_FAILED,
        LIKE_REMOVED,
        LIKE_NOT_REMOVED,
        LIKE_REMOVED_FAIL,
        FOLLOWER_REMOVED,
        FOLLOWING_REMOVED,
        FOLLOWER_REMOVED_FAIL,
        FOLLOWING_REMOVED_FAIL,
        IDLE
    }


}
