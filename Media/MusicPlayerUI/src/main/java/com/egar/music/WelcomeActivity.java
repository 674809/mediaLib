package com.egar.music;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.egar.music.activity.MusicListActivity;
import com.egar.music.activity.base.BaseUsbLogicActivity;
import com.egar.music.utils.MusicPreferUtils;

import juns.lib.android.utils.Logs;

public class WelcomeActivity extends BaseUsbLogicActivity {
    //TAG
    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Logs.i(TAG, "onCreate()");
        init();
    }

    private void init() {
        Logs.i(TAG, "init()");
        TextView tvContent = (TextView) findViewById(R.id.tv_content);
        tvContent.post(new Runnable() {
            @Override
            public void run() {
                //  startActivity(new Intent(WelcomeActivity.this, TestUiActivity.class));
                boolean isUsbMounted = isUsbMounted();
                boolean isDebugMode = MusicPreferUtils.getNoUDiskToastFlag(false) == 0;
                Logs.i(TAG, "isUsbMounted: " + isUsbMounted + "; isDebugMode: " + isDebugMode);
                if (isUsbMounted || isDebugMode) {
                    startActivity();
                    finish();
                } else {
                    toastMsg();//Will execute finish operation after dismiss.
                }
            }
        });
    }

    /**
     * Start activity of top stack.
     */
    private void startActivity() {
        try {
            App app = (App) getApplication();
            Activity currActivity = app.current();
            if (currActivity == null) {
                Logs.i(TAG, "start music list page.");
                startActivity(new Intent(WelcomeActivity.this, MusicListActivity.class));
            } else {
                Logs.i(TAG, "start activity of top stack !!!");
                startActivity(new Intent(WelcomeActivity.this, currActivity.getClass()));
            }
        } catch (Exception e) {
            Logs.i(TAG, "startActivity() >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        Logs.i(TAG, "finish()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.i(TAG, "onDestroy()");
    }
}
