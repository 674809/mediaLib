package com.egar.audio.service.presenter;

public interface IVolumeFadePresenter {
    interface IVolumeFadeListener {
        void onVolumeFadeChanged(float leftVolume, float rightVolume);
    }
    long getVolumeFadePeriod();
    void volumeResetAndFadeIn();
    void volumeResetAndFadeOut();
    void volumeFadeDestroy();
}
