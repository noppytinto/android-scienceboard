package com.nocorp.scienceboard.system;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionManager {
    private static ConnectionManager singletonInstance;


    //-------------------------------------------------------------------- CONSTRUCTORS

    private ConnectionManager() {}




    //-------------------------------------------------------------------- METHODS


    public static ConnectionManager getInstance() {
        if(singletonInstance==null)
            singletonInstance = new ConnectionManager();

        return singletonInstance;
    }

    public static boolean getInternetStatus(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


}// end ConnectionManager
