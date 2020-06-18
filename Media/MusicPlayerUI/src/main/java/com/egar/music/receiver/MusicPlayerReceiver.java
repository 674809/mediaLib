package com.egar.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.egar.music.App;
import com.egar.music.WelcomeActivity;
import com.egar.music.utils.MusicPreferUtils;

import juns.lib.android.utils.Logs;
import juns.lib.media.action.MediaActions;

/**
 * Music player receiver
 *
 * @author Jun.Wang
 */
public class MusicPlayerReceiver extends BroadcastReceiver {
    //TAG
    private static final String TAG = "MusicPlayerReceiver";

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
                case MediaActions.OPEN_MUSIC_LOGS:
                    Logs.switchEnable(false);
                    break;
                case MediaActions.SWITCH_DEBUG_MODE:
                    MusicPreferUtils.getNoUDiskToastFlag(true);
                    break;
                case MediaActions.MUSIC_OPEN_PLAY:
                case MediaActions.MUSIC_OPEN:
                    openPlayer(context);
                    break;
                case MediaActions.MUSIC_CLOSE:
                    closePlayer(context);
                    break;
            }
        }
    }

    private void openPlayer(Context context) {
        try {
            context.startActivity(new Intent(context, WelcomeActivity.class));
        } catch (Exception e) {
            Logs.i(TAG, "openPlayer() >> [e: " + e.getMessage());
        }
    }

    private void closePlayer(Context context) {
        try {
            App app = (App) context.getApplicationContext();
            app.exitApp();
        } catch (Exception e) {
            Logs.i(TAG, "closePlayer() >> [e: " + e.getMessage());
        }
    }
}
