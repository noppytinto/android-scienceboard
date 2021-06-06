package com.nocorp.scienceboard.system;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;


import com.nocorp.scienceboard.R;

public class ScienceBoardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        checkDarkMode();
        ThreadManager.init();
        MyOkHttpClient.init(this);
//        initAmplify();
        Log.d(ScienceBoardApplication.class.getSimpleName(), "SCIENCE_BOARD - onCreate(): application initilized");
    }

//    private void initAmplify() {
//        try {
//            Amplify.configure(getApplicationContext());
//            Log.d("MyAmplifyApp", "Initialized Amplify");
//        } catch (AmplifyException error) {
//            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
//        }
//    }

    private void checkDarkMode() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_app_theme_key), Context.MODE_PRIVATE);
        boolean defaultValue = getResources().getBoolean(R.bool.preference_app_theme_default_value_key);
        boolean darkModeEnabled = sharedPref.getBoolean(getString(R.string.preference_app_theme_key), defaultValue);

        if(darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
