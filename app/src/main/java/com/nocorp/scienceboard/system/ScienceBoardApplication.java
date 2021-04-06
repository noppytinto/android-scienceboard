package com.nocorp.scienceboard.system;

import android.app.Application;
import android.util.Log;
import com.nocorp.scienceboard.utility.MyOkHttpClient;

public class ScienceBoardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThreadManager.init();
        MyOkHttpClient.init(this);
        Log.d(ScienceBoardApplication.class.getSimpleName(), "SCIENCE_BOARD - onCreate(): application initilized");
    }
}
