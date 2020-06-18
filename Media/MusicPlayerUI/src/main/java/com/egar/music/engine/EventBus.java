package com.egar.music.engine;

import java.util.HashMap;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;

public class EventBus implements EventBusDelegate {
    //TAG
    private static final String TAG = "StackManager";

    //Callback
    private Map<String, EventBusCallback> mMapCallbacks;

    @Override
    public void addEbCallback(EventBusCallback callback) {
        Logs.i(TAG, "addEbCallback(" + callback + ")");
        if (callback != null) {
            if (mMapCallbacks == null) {
                mMapCallbacks = new HashMap<>();
            }
            mMapCallbacks.put(callback.getClass().getName(), callback);
        }
    }

    @Override
    public void removeEbCallback(EventBusCallback callback) {
        Logs.i(TAG, "removeEbCallback(" + callback + ")");
        if (mMapCallbacks != null && callback != null) {
            mMapCallbacks.remove(callback.getClass().toString());
        }
    }

    @Override
    public void publishEbCollect(int position, ProAudio media) {
        Logs.i(TAG, "publishEbCollect(" + position + "," + media + ")");
        if (mMapCallbacks != null) {
            for (EventBusCallback callback : mMapCallbacks.values()) {
                callback.onEbCollect(position, media);
            }
        }
    }
}
