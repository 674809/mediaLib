package com.egar.music.engine;

import juns.lib.media.bean.ProAudio;

public interface EventBusDelegate {
    /**
     * Event response.
     */
    interface EventBusCallback {
        void onEbCollect(int position, ProAudio media);
    }

    void addEbCallback(EventBusCallback callback);

    void removeEbCallback(EventBusCallback callback);

    void publishEbCollect(int position, ProAudio media);
}
