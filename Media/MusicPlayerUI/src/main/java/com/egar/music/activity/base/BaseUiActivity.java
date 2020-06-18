package com.egar.music.activity.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.egar.music.App;
import com.egar.music.engine.EventBus;
import com.egar.music.engine.EventBusDelegate;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;

public abstract class BaseUiActivity extends BaseThemeActivity implements EventBusDelegate {
    //TAG
    private static final String TAG = "BaseUiActivity";

    /**
     * Theme flag from Settings.
     */
    private int mThemeFlag = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        //initThemeController(Settings.System.getUriFor("theme_setting"));
        addSkinFileInfo(1, "ios", "skins/MusicPlayer_Skin_IOS-release.apk");
    }

    @Override
    public int getThemeValue(boolean isCallByController) {
        if (!isCallByController) {
            switch (mThemeFlag) {
                case 0:
                    mThemeFlag = 1;
                    break;
                case 1:
                default:
                    mThemeFlag = 0;
                    break;
            }
            return mThemeFlag;
        }
        return super.getThemeValue(true);
    }

    @Override
    public void onThemeChanged(int oldThemeType, int currThemeType) {
        super.onThemeChanged(oldThemeType, currThemeType);
        //Usually is default, unless you want override content yourself.
    }

    public void toggleSkin() {
        onThemeChanged(-1, getThemeValue(false));
    }

    /**
     * Add activity to stack.
     *
     * @param activity - Target activity.
     */
    public void addToStack(Activity activity) {
        try {
            Logs.i(TAG, "addToStack(" + activity + ")");
            App app = (App) getApplication();
            app.add(activity);
        } catch (Exception e) {
            Logs.i(TAG, "addToStack() >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove activity from stack.
     *
     * @param activity - Target activity.
     */
    public void removeFromStack(Activity activity) {
        try {
            Logs.i(TAG, "removeFromStack(" + activity + ")");
            App app = (App) getApplication();
            app.remove(activity);
        } catch (Exception e) {
            Logs.i(TAG, "removeFromStack() >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void exitApplication() {
        try {
            Logs.i(TAG, "exitApplication()");
            App app = (App) getApplication();
            app.exitApp();
        } catch (Exception e) {
            Logs.i(TAG, "exitApplication() >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * {@link EventBus#addEbCallback(EventBusCallback)}
     */
    @Override
    public void addEbCallback(EventBusCallback callback) {
        try {
            Logs.i(TAG, "addEbCallback(" + callback + ")");
            App app = (App) getApplication();
            app.addEbCallback(callback);
        } catch (Exception e) {
            Logs.i(TAG, "addEbCallback() >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * {@link EventBus#removeEbCallback(EventBusCallback)}
     */
    @Override
    public void removeEbCallback(EventBusCallback callback) {
        try {
            Logs.i(TAG, "removeEbCallback(" + callback + ")");
            App app = (App) getApplication();
            app.removeEbCallback(callback);
        } catch (Exception e) {
            Logs.i(TAG, "removeEbCallback() >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * {@link EventBus#publishEbCollect(int, ProAudio)}
     */
    @Override
    public void publishEbCollect(int position, ProAudio media) {
        try {
            Logs.i(TAG, "publishEbCollect(" + position + "," + media.getMediaUrl() + ")");
            App app = (App) getApplication();
            app.publishEbCollect(position, media);
        } catch (Exception e) {
            Logs.i(TAG, "publishEbCollect() >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
