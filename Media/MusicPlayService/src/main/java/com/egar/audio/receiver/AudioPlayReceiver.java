package com.egar.audio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.egar.audio.service.AudioPlayService;

import juns.lib.android.utils.Logs;
import juns.lib.media.action.MediaActions;

/**
 * Audio play receiver
 *
 * @author Jun.Wang
 */
public class AudioPlayReceiver extends BroadcastReceiver {
    //TAG
    private static final String TAG = "AudioPlayReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logs.i(TAG, "action : " + action);
        //Let service process logic
        if (action != null) {
            switch (action) {
                case MediaActions.BOOT_COMPLETED:
                    Logs.switchEnable(true);
                    break;
                case MediaActions.OPEN_AUDIO_LOGS:
                    Logs.switchEnable(false);
                    break;
                default:
                    startAudioPlayService(context, intent);
                    break;
            }
        }
    }

    /**
     * Start scanner service
     *
     * @param context {@link Context}
     * @param data    {@link Intent}
     */
    public void startAudioPlayService(Context context, Intent data) {
        Intent intentScan = new Intent(context, AudioPlayService.class);
        intentScan.putExtra(AudioPlayService.PARAM_ACTION, data);
        context.startService(intentScan);
    }
}
