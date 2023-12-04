package com.ekc.ekccollector.collector.model.singleton;

import android.os.Handler;
import android.os.HandlerThread;

public class ThreadSingleton {
    private final String threadName = "ThreadSingleton";

    private HandlerThread handlerThread;
    private Handler handler;

    public static ThreadSingleton getInstance() {
        return new ThreadSingleton();
    }

    public void initBgThread() {
        try {
            handlerThread = new HandlerThread(threadName);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dispatchBgThread() {
        try {
            handlerThread.quitSafely();
            handlerThread = null;
            handler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HandlerThread getHandlerThread() {
        return handlerThread;
    }

    public void setHandlerThread(HandlerThread handlerThread) {
        this.handlerThread = handlerThread;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
