package com.egar.audio;

import android.app.Application;
import android.content.Context;

import com.egar.audio.utils.ALogs;
import com.egar.audio.utils.AudioPreferUtils;

/**
 * Scanner application
 *
 * @author Jun.Wang
 */
public class App extends Application {
    //TAG
    private static final String TAG = "PlayServiceApp";

    /**
     * Application context object
     */
    private Context mAppCxt;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        //Application context
        mAppCxt = getApplicationContext();

        //Initialize juns.lib.android.utils.Logs
        ALogs.sInit(mAppCxt);
        //
        AudioPreferUtils.init(mAppCxt);
    }
}
