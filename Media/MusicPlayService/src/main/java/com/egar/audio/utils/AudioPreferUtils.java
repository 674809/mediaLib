package com.egar.audio.utils;

import android.content.Context;

import juns.lib.android.utils.SpUtils;
import juns.lib.media.flags.PlayMode;

/**
 * Audio preference tool utils.
 *
 * @author Jun.Wang
 */
public class AudioPreferUtils {
    public static void init(Context context) {
        SpUtils.instance().init(context);
    }

    /**
     * Get audio play mode
     *
     * @param isSet    true - Cache mode value.
     * @param playMode {@link PlayMode}
     * @return js.lib.android.media.PlayMode
     */
    public static int getPlayMode(boolean isSet, int playMode) {
        final String PREFER_KEY = "AUDIO_PLAY_MODE";
        if (isSet) {
            SpUtils.instance().saveInt(PREFER_KEY, playMode);
        }
        return SpUtils.instance().getInt(PREFER_KEY, PlayMode.LOOP);
    }

    /**
     * Last Played Media Information
     *
     * @return String[] [0]mediaUrl,[1]progress
     */
    public static String[] getLastPlayedMediaInfo(boolean isSet, String mediaUrl, int progress) {
        final String PREFER_KEY_MEDIA_URL = "AUDIO_LAST_PLAYED_MEDIA_URL";
        final String PREFER_KEY_MEDIA_PROGRESS = "AUDIO_LAST_PLAYED_MEDIA_PROGRESS";

        //Save
        if (isSet) {
            SpUtils.instance().saveString(PREFER_KEY_MEDIA_URL, mediaUrl);
            SpUtils.instance().saveInt(PREFER_KEY_MEDIA_PROGRESS, progress);
        }

        //Get
        String[] mediaInfos = new String[2];
        mediaInfos[0] = SpUtils.instance().getString(PREFER_KEY_MEDIA_URL, "");
        mediaInfos[1] = SpUtils.instance().getInt(PREFER_KEY_MEDIA_PROGRESS, 0).toString();
        return mediaInfos;
    }
}
