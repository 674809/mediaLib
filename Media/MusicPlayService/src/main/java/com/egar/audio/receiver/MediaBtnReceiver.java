package com.egar.audio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.egar.audio.service.AudioPlayService;

/**
 * Media button broadcast receiver.
 * <p>1. Register {@link MediaBtnReceiver} with action:"android.intent.action.MEDIA_BUTTON" in your 'AndroidManifest.xml'</p>
 * <p>2. Register in where you want use media button {@link android.media.AudioManager#registerMediaButtonEventReceiver}</p>
 *
 * @author Jun.Wang
 */
public class MediaBtnReceiver extends BroadcastReceiver {
    //TAG
    private static final String TAG = "MediaBtnReceiver";

    public interface MediaBtnListener {
        void onGotMediaKey(KeyEvent event);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "action: " + action);

        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            startAudioPlayService(context, intent);
        }
    }

    /**
     * Start scanner service
     *
     * @param context {@link Context}
     * @param data    {@link Intent}
     */
    public void startAudioPlayService(Context context, Intent data) {
        Intent intent = new Intent(context, AudioPlayService.class);
        intent.putExtra(AudioPlayService.PARAM_ACTION, data);
        context.startService(intent);
    }
}
