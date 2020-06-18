package com.egar.music.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.egar.music.R;
import com.egar.music.activity.base.BaseUiActivity;

public class TestUiActivity extends BaseUiActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ui);
        findViewById(R.id.iv_ui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSkin();
            }
        });
    }

    @Override
    public void onThemeChanged(int oldThemeType, int currThemeType) {
        super.onThemeChanged(oldThemeType, currThemeType);
    }

    @Override
    public void onGotKey(int keyCode) {
    }
}
