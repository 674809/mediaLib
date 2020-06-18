package com.egar.music.activity.base;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.egar.music.api.activity.ApiAudioFragActivity;

import java.util.List;

import juns.lib.media.flags.KeyCodes;

public abstract class BaseExtendActionsActivity extends ApiAudioFragActivity {
    //TAG
    private static final String TAG = "BaseExtendActivity";

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                break;
            case KeyEvent.ACTION_UP:
                Log.i(TAG, "dispatchKeyEvent() -ACTION_UP-");
                onGotKey(keyCode);
                break;
        }

        switch (keyCode) {
            case KeyCodes.KEYCODE_DPAD_LEFT:
            case KeyCodes.KEYCODE_DPAD_RIGHT:
            case KeyCodes.KEYCODE_ENTER:
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    public abstract void onGotKey(int keyCode);

    /**
     * Is Playing Media
     */
    public boolean isPlayingSameMedia(String mediaUrl) {
        boolean isPlayingSameMedia = isPlaying() && TextUtils.equals(getCurrMediaPath(), mediaUrl);
        Log.i(TAG, "isPlayingSameMedia : " + isPlayingSameMedia);
        return isPlayingSameMedia;
    }

    protected boolean isActActive() {
        return !isFinishing() && !isDestroyed();
    }

    @Override
    public void onScanStateChanged(int state) {
        super.onScanStateChanged(state);
    }

    @Override
    public void onGotDeltaMedias(List listMedias) {
        super.onGotDeltaMedias(listMedias);
    }

    @Override
    public void onPlayStateChanged(int playStateValue) {
        super.onPlayStateChanged(playStateValue);
    }
}
