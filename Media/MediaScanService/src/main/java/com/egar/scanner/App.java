package com.egar.scanner;

import android.app.Application;
import android.content.Context;

import com.egar.scanner.utils.SLogs;

import juns.lib.media.utils.ScannerFileUtils;

import juns.lib.android.utils.Logs;

/**
 * Scanner application
 */
public class App extends Application {
    //TAG
    private static final String TAG = "ScannerApp";

    /**
     * Application context object
     */
    private Context mAppCxt;

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.i(TAG, "onCreate()");
        init();
    }

    private void init() {
        //Application context
        mAppCxt = getApplicationContext();

        //Initialize juns.lib.android.utils.Logs
        SLogs.sInit(mAppCxt);
        //Initialize com.egar.scanner.engine.ScannerFileUtils
        ScannerFileUtils.init(mAppCxt);
    }
}
