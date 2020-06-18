package com.egar.audio.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.egar.audio.receiver.AudioPlayReceiver;
import com.egar.audio.service.presenter.IPlayPresenter;
import com.egar.audio.service.presenter.impl.PlayPresenter;

import juns.lib.android.utils.Logs;
import juns.lib.media.action.MediaActions;

public class AudioPlayService extends AudioPlayServiceBase {
    //TAG
    private static final String TAG = "AudioPlayService";

    //Parameters key
    public static final String PARAM_ACTION = "ACTION";

    /**
     * {@link IPlayPresenter}
     */
    private IPlayPresenter mPlayPresenter;

    private IPlayPresenter getPlayPresenter() {
        if (mPlayPresenter == null) {
            mPlayPresenter = new PlayPresenter(this);
        }
        return mPlayPresenter;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        //Keep service alive.
        keepAlive();
    }

    private void keepAlive() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind(intent)");
        return getPlayPresenter().asBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Intent dataParam = intent.getParcelableExtra(PARAM_ACTION);
            if (dataParam != null) {
                String action = dataParam.getAction();
                if (action != null) {
                    intent.removeExtra(PARAM_ACTION);
                    processAction(action, dataParam);
                }
            }
        }
        //Bind scanning service
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Process action from {@link AudioPlayReceiver}
     */
    private void processAction(String action, Intent data) {
        try {
            Logs.i(TAG, "action : " + action);
            switch (action) {
                //MEDIA_BUTTON
                case MediaActions.MEDIA_BUTTON: {
                    KeyEvent keyEvent = data.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                    if (keyEvent != null && keyEvent.getAction() == MotionEvent.ACTION_UP) {
                        getPlayPresenter().onGotMediaKey(keyEvent);
                    }
                }
                break;

                // Voice commands
                default:
                    getPlayPresenter().onGotVoiceCmd(action, data);
                    break;
            }
        } catch (Exception e) {
            Logs.i(TAG, "processAction() >> e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // Destroy PlayPresenter
        destroyPresenter();
        super.onDestroy();
    }

    private void destroyPresenter() {
        if (mPlayPresenter != null) {
            mPlayPresenter.destroy();
            mPlayPresenter = null;
        }
    }
}