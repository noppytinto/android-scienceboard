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
        ThreadManager.init();
        MyOkHttpClient.init(this);
        Log.d(ScienceBoardApplication.class.getSimpleName(), "SCIENCE_BOARD - onCreate(): application initilized");
    }


}
