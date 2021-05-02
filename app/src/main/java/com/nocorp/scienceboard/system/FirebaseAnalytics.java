package com.nocorp.scienceboard.system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Movie;
import android.os.Bundle;


public class FirebaseAnalytics {
    private static FirebaseAnalytics singletonInstance;
    private boolean haveUserConsensus; // user consensus for log collection





    //--------------------------------------------------------- CONSTRUCTORS

    private FirebaseAnalytics() {
        //
    }




    //--------------------------------------------------------- GETTERS/SETTERS

    public void setUserConsent(boolean consensus) {
        this.haveUserConsensus = consensus;
    }

    public static FirebaseAnalytics getInstance() {
        if (singletonInstance == null)
            singletonInstance = new FirebaseAnalytics();
        return singletonInstance;
    }




    //--------------------------------------------------------- METHODS

    public void logScreenEvent(Object screenClass, String screenName, Context context) {
        if(haveUserConsensus) {
            @SuppressLint("MissingPermission")
            com.google.firebase.analytics.FirebaseAnalytics mFirebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
            Bundle params = new Bundle();
            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_NAME, screenName);
            mFirebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW, params);
        }
    }

    public void logSearchTerm(String term, Object screenClass, Context context) {
        if(haveUserConsensus) {
            if(term==null || term.isEmpty()) return;

            // send to firebase analytics
            @SuppressLint("MissingPermission")
            com.google.firebase.analytics.FirebaseAnalytics mFirebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
            //throw new RuntimeException("Test Crash"); // Force a crash for Crashlytics
            Bundle params = new Bundle();
            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SEARCH_TERM, term);
            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.SEARCH, params);
        }
    }

//    public void logSelectedMovie(Movie movie, String itemCategory, Object screenClass, Context context) {
//        if(haveUserConsensus) {
//            // send to firebase analytics
//            @SuppressLint("MissingPermission")
//            com.google.firebase.analytics.FirebaseAnalytics mFirebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
//            Bundle params = new Bundle();
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY, itemCategory);
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movie.getTmdbID()));
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME, movie.getTitle());
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
////        Bundle params = new Bundle();
////        params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});
//            mFirebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_ITEM, params);
//        }
//    }


//    public void logSelectedSearchedMovie(MovieSearchResult movie, String itemCategory, Object screenClass, Context context) {
//        if(haveUserConsensus) {
//            // send to firebase analytics
//            @SuppressLint("MissingPermission")
//            com.google.firebase.analytics.FirebaseAnalytics mFirebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
//            Bundle params = new Bundle();
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY, itemCategory);
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movie.getTmdbID()));
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME, movie.getTitle());
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
////        Bundle params = new Bundle();
////        params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});
//            mFirebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_ITEM, params);
//        }
//    }
//
//    public void logSelectedViewAllMovie(Movie movie, String itemCategory, Object screenClass, Context context) {
//        if(haveUserConsensus) {
//            // send to firebase analytics
//            @SuppressLint("MissingPermission")
//            com.google.firebase.analytics.FirebaseAnalytics mFirebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
//            Bundle params = new Bundle();
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY, itemCategory);
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movie.getTmdbID()));
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME, movie.getTitle());
//            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
////        Bundle params = new Bundle();
////        params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});
//            mFirebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_ITEM, params);
//        }
//    }


    public void logLoginEvent(String method, Context context) {
        @SuppressLint("MissingPermission")
        com.google.firebase.analytics.FirebaseAnalytics mFirebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.METHOD, method);
        mFirebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.LOGIN, bundle);
    }



}// end FirebaseAnalytics class
