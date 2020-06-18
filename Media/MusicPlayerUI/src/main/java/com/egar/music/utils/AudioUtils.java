package com.egar.music.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.egar.music.R;

import juns.lib.java.utils.EmptyUtil;
import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.player.MediaUtils;

public class AudioUtils {
    //TAG
    private static final String TAG = "AudioUtils";

    /**
     * Set media cover Image
     */
    public static void setMediaCover(ImageView ivCover, ProAudio media) {
        if (EmptyUtil.isEmpty(media.getCoverUrl())) {
            ivCover.setImageResource(R.drawable.bg_cover_music_udisk);
        } else {
            ivCover.setImageURI(Uri.parse(media.getCoverUrl()));
        }
    }

    /**
     * Get Media Title
     */
    public static String getMediaTitle(Context context, int position, ProAudio media, boolean isContainSuffix) {
        String title = "";
        try {
            if (position >= 0) {
                title = position + ". ";
            }
            title += getUnKnowOnNull(context, media.getTitle());
            if (isContainSuffix) {
                title += MediaUtils.getSuffix(media.getMediaUrl());
            }
        } catch (Exception e) {
            title = "";
        }
        return title;
    }

    /**
     * Return String or UnKnow
     */
    public static String getUnKnowOnNull(Context cxt, String str) {
        if (EmptyUtil.isEmpty(str)) {
            return cxt.getString(R.string.unknow);
        }
        return str;
    }

    /**
     * Toast Play
     * Error
     */
    public static void toastPlayError(Context cxt, String mediaTitle) {
        String errorMsg = String.format(cxt.getString(R.string.play_error), mediaTitle);
        Logs.i(TAG, errorMsg);
//        Toast.makeText(cxt, errorMsg, Toast.LENGTH_LONG).show();
    }
}
