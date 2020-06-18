package com.egar.music.api.utils;

import android.content.Context;
import android.egar.CarManager;
import android.egar.EventProxyClient;
import android.egar.MediaStatus;
import android.provider.Settings;
import android.util.Log;

import com.egar.CarSettings.CarSettings;

/**
 * {@link Settings} util
 */
public class SettingsSysUtil {
    //TAG
    private static final String TAG = "SettingsSysUtil";

    /**
     * Set music state
     *
     * @param context {@link Context}
     * @param state   {@link MediaStatus#MEDIA_STATUS_STOP}
     *                or {@link MediaStatus#MEDIA_STATUS_PAUSE}
     *                or {@link MediaStatus#MEDIA_STATUS_PLAYING}
     */
    public static void setAudioState(Context context, int state) {
        try {
            Log.i(TAG, "setAudioState(Context," + state + ")");
            //Get client.
            CarManager carManager = new CarManager(context);
            EventProxyClient client = carManager.getEventProxy(context);
            //Set status.
            MediaStatus mediaStatus = new MediaStatus();
            mediaStatus.mMediaType = MediaStatus.MEDIA_TYPE_LOCALMUSIC;
            mediaStatus.mMediaStatus = state;
            client.setMediaStatus(mediaStatus);
        } catch (Exception e) {
            Log.i(TAG, "setAudioState >> [e: " + e.getMessage());
        }
    }

    /**
     * Get theme value
     *
     * @return int-0 默认主题 ; 1 苹果主题
     */
    public static int getThemeVal(Context context) {
        return CarSettings.CarSetting.getInt(context.getContentResolver(), CarSettings.CarSetting.THEME_SETTING, 0);
    }
}
