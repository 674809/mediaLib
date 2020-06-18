package com.egar.audio.activity;

import android.util.Log;
import android.view.KeyEvent;

import com.egar.music.api.activity.ApiAudioFragActivity;

import juns.lib.media.flags.KeyCodes;

public abstract class BaseTestActivity extends ApiAudioFragActivity {
    //TAG
    private static final String TAG = "BaseFragTest";

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                break;
            case KeyEvent.ACTION_UP:
                Log.i(TAG, "dispatchKeyEvent(" + event + ")");
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
}
