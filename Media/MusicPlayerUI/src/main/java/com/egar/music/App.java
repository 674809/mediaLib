package com.egar.music;

import android.app.Activity;
import android.content.Context;

import com.egar.music.engine.EventBus;
import com.egar.music.engine.EventBusDelegate;
import com.egar.music.engine.StackManager;
import com.egar.music.engine.StackManagerDelegate;
import com.egar.music.utils.MLogs;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import xskin.widget.base.BaseApplication;

public class App extends BaseApplication implements StackManagerDelegate, EventBusDelegate {
    //TAG
    private static final String TAG = "App";

    /**
     * Application context object
     */
    private Context mAppCxt;

    /**
     * Event bus delegate.
     * <P>will used to create {@link EventBusDelegate} object.</P>
     */
    private EventBusDelegate mEbDelegate;

    /**
     * Stack manger delegate.
     * <P>will used to create {@link StackManager} object.</P>
     */
    private StackManagerDelegate mStackManager;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        //Application context
        mAppCxt = getApplicationContext();

        //Initialize juns.lib.android.utils.Logs
        //SpUtils.instance().init(mAppCxt);
        MLogs.sInit(mAppCxt);

        // Initialize EventBusDelegate
        mEbDelegate = new EventBus();

        // Initialize StackManager.
        mStackManager = new StackManager();
    }

    /**
     * {@link EventBus#addEbCallback(EventBusCallback)}
     */
    @Override
    public void addEbCallback(EventBusCallback callback) {
        Logs.i(TAG, "addEbCallback(" + callback + ")");
        if (mEbDelegate != null) {
            mEbDelegate.addEbCallback(callback);
        }
    }

    /**
     * {@link EventBus#removeEbCallback(EventBusCallback)}
     */
    @Override
    public void removeEbCallback(EventBusCallback callback) {
        Logs.i(TAG, "removeEbCallback(" + callback + ")");
        if (mEbDelegate != null) {
            mEbDelegate.removeEbCallback(callback);
        }
    }

    /**
     * {@link EventBus#publishEbCollect(int, ProAudio)}
     */
    @Override
    public void publishEbCollect(int position, ProAudio media) {
        Logs.i(TAG, "publishEbCollect(" + position + "," + media + ")");
        if (mEbDelegate != null) {
            mEbDelegate.publishEbCollect(position, media);
        }
    }

    /**
     * {@link StackManager#add(Activity)}
     */
    @Override
    public void add(Activity activity) {
        Logs.i(TAG, "add(" + activity + ")");
        if (mStackManager != null) {
            mStackManager.add(activity);
        }
    }

    /**
     * {@link StackManager#remove(Activity)}
     */
    @Override
    public void remove(Activity activity) {
        Logs.i(TAG, "remove(" + activity + ")");
        if (mStackManager != null) {
            mStackManager.remove(activity);
        }
    }

    /**
     * {@link StackManager#exitApp()}
     */
    @Override
    public void exitApp() {
        Logs.i(TAG, "exitApp()");
        if (mStackManager != null) {
            mStackManager.exitApp();
        }
    }

    /**
     * {@link StackManager#current()}
     */
    @Override
    public Activity current() {
        if (mStackManager != null) {
            return mStackManager.current();
        }
        return null;
    }
}
