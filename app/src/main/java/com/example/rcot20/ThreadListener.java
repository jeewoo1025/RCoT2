package com.example.rcot20;

import android.os.Handler;
import android.util.Log;

public class ThreadListener {
    private static SubThread subThread = null;        // critical section....

    public static SubThread getThread() {
        if(subThread == null) {
            Log.d("소켓", "subThread == null, new Thread");
            subThread = new SubThread();
            subThread.setDaemon(true);
        }
        return subThread;
    }

    public static void getThreadStarted() {
        if(subThread.getState() == Thread.State.NEW) {
            Log.d("소켓", "subThread state is NEW!");
            subThread.setRunning(true);
            subThread.start();
        }
    }

    public static void closeThread() {
        subThread.setRunning(false);
    }

    public static void setHandler(Handler handler) {
        subThread.setHandler(handler);
    }
}
