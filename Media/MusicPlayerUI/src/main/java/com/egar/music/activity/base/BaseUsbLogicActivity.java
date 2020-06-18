package com.egar.music.activity.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.egar.music.view.ToastMsgController;

import java.util.Map;

import juns.lib.android.activity.BaseFragActivity;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.utils.SDCardUtils;

/**
 * Usb logic process
 */
public abstract class BaseUsbLogicActivity extends BaseFragActivity {
    //TAG
    private static final String TAG = "BaseUsbLogicActivity";

    private ToastMsgController mToastMsgController;

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        mToastMsgController = new ToastMsgController(this);
    }

    protected boolean isUsbMounted() {
        Map<String, StorageDevice> mapStorage = SDCardUtils.getMapMountedUsb(this);
        return !EmptyUtil.isEmpty(mapStorage);
    }

    @Override
    protected void onHomeKeyClick() {
        super.onHomeKeyClick();
        if (mToastMsgController != null) {
            mToastMsgController.onHomeKeyClick();
        }
    }

    @Override
    public void finish() {
        Log.i(TAG, "finish()");
        if (mToastMsgController != null) {
            mToastMsgController.finish();
        }
        super.finish();
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        if (mToastMsgController != null) {
            mToastMsgController.onDestroy();
        }
        super.onDestroy();
    }

    /**
     * Toast message
     * <p>Will execute finish after some time.</p>
     */
    protected void toastMsg() {
        Log.i(TAG, "toastMsg()");
        if (mToastMsgController != null) {
            mToastMsgController.toastMsg();
        }
    }
}
