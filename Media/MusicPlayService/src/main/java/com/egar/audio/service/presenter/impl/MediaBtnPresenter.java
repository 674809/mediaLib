package com.egar.audio.service.presenter.impl;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.support.annotation.NonNull;

public class MediaBtnPresenter {
    private String TAG = "MediaBtnPresenter";
    private Context mContext;
    private AudioManager mAudioManager;
    private ComponentName mComponentName;
    private RemoteControlClient mRemoteControlClient;

    public MediaBtnPresenter(@NonNull Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * @param clsMediaBtnReceiver The name of the class inside of <var>pkg</var> that implements the component.  Can not be null.
     */
    @SuppressWarnings("deprecation")
    public void register(@NonNull String clsMediaBtnReceiver) {
        mComponentName = new ComponentName(mContext.getPackageName(), clsMediaBtnReceiver);
        mAudioManager.registerMediaButtonEventReceiver(mComponentName);

        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setComponent(mComponentName);

        PendingIntent pi = PendingIntent.getBroadcast(mContext/*context*/, 0/*requestCode*/, i/*intent*/, 0/*flags*/);
        mRemoteControlClient = new RemoteControlClient(pi);
        mAudioManager.registerRemoteControlClient(mRemoteControlClient);
    }

    @SuppressWarnings("deprecation")
    public void unregister() {
        mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
    }
}
