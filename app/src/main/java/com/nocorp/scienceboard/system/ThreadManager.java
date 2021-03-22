package com.nocorp.scienceboard.system;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {
    private static ThreadManager singletonInstance = null;

    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES;
    // Instantiates the queue of Runnables as a LinkedBlockingQueue
    private final BlockingQueue<Runnable> workQueue;
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    // Creates a thread pool manager
    private ThreadPoolExecutor threadPoolExecutor;

    private ThreadManager() {
        NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
//        NUMBER_OF_CORES = 4;
        workQueue = new LinkedBlockingQueue<Runnable>();
        threadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                workQueue
        );
    }

    public ThreadPoolExecutor getExecutor() {
        return threadPoolExecutor;
    }


    public static void init() {
        if(singletonInstance == null){
            singletonInstance = new ThreadManager();
            Log.d(ThreadManager.class.getSimpleName(), "init(): success");
        }
    }

    public static synchronized ThreadManager getInstance() {
        init();
        return singletonInstance;
    }

    public void runTaskInPool(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

}// end ThreadManager class

