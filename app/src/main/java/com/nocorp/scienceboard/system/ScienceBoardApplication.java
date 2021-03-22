package com.nocorp.scienceboard.system;

import android.app.Application;
import android.util.Log;

public class ScienceBoardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThreadManager.init();
        MyOkHttpClient.init(this);
        Log.d(ScienceBoardApplication.class.getSimpleName(), "onCreate(): application initilized");
    }
}
