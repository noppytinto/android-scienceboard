package com.nocorp.scienceboard.system;

import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nocorp.scienceboard.R;


public class RemoteConfigServer {
    private final String TAG = this.getClass().getSimpleName();
    private static RemoteConfigServer singletonInstance = null;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    private RemoteConfigListener listener;

    public interface RemoteConfigListener {
        public void onRemoteParamsLoaded(boolean taskIsSuccessful);
    }



    //------------------------------------------------------- CONSTRUCTORS

    private RemoteConfigServer() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        // NOTE: the deault interval il 12h
        //.setMinimumFetchIntervalInSeconds(0)
    }



    //------------------------------------------------------- GETTERS/SETTERS

    public static RemoteConfigServer getInstance() {
        if (singletonInstance == null)
            singletonInstance = new RemoteConfigServer();

        return singletonInstance;
    }

    public void setListener(RemoteConfigListener remoteConfigListener) {
        listener = remoteConfigListener;
    }

    public String getTest() {
        Log.d("RemoteConfig: ", "getTest() called");
        return mFirebaseRemoteConfig.getString("test");
    }




    //------------------------------------------------------- METHODS

    public void loadConfigParams() {
//        mFirebaseRemoteConfig.fetch(0);
//        mFirebaseRemoteConfig.activate();

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean updated = task.getResult();
                // notify listener
                listener.onRemoteParamsLoaded(true);
            } else
                listener.onRemoteParamsLoaded(false);
        });
    }// end loadConfigParams()

    public void releaseListener() {
        listener = null;
    }

}// end RemoteConfig class