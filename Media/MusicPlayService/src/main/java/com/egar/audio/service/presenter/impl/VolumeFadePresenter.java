package com.egar.audio.service.presenter.impl;

import android.os.Handler;
import android.util.Log;

import com.egar.audio.service.presenter.IVolumeFadePresenter;

/**
 * Volume fade in/out presenter.
 *
 * @author Jun.Wang
 */
public class VolumeFadePresenter implements IVolumeFadePresenter {
    //TAG
    private static final String TAG = "VolumeFadePresenter";

    //
    private Handler mmFadeHandler = new Handler();
    private float mmVolume = 0.2f;
    private final float VOLUME_MAX = 1.0f, VOLUME_MIN = 0.0f, STEP_VOLUME = 0.2f;

    //ms
    private final long SINGLE_FADE_PERIOD = 100;

    /**
     * {@link IVolumeFadeListener}
     */
    private IVolumeFadeListener mVolumeFadeListener;

    VolumeFadePresenter(IVolumeFadeListener l) {
        mVolumeFadeListener = l;
    }

    /**
     * Get duration of fade in/out. Unit is milliseconds.
     */
    @Override
    public long getVolumeFadePeriod() {
        return (long) (SINGLE_FADE_PERIOD * (VOLUME_MAX / STEP_VOLUME));
    }

    /**
     * Reset to minimum volume , then add to maximum step by step.
     */
    @Override
    public void volumeResetAndFadeIn() {
        mmFadeHandler.removeCallbacksAndMessages(null);
        mmVolume = VOLUME_MIN;
        loopFadeIn();
    }

    private void loopFadeIn() {
        setPlayerVolume();
        Log.i(TAG, "loopFadeIn() - mmVolume : " + mmVolume);
        if (mmVolume >= VOLUME_MAX) {
            mmVolume = VOLUME_MAX;
            return;
        }

        //
        mmVolume = (float) (Math.round((mmVolume + STEP_VOLUME) * 100)) / 100;
        mmFadeHandler.removeCallbacksAndMessages(null);
        mmFadeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loopFadeIn();
            }
        }, SINGLE_FADE_PERIOD);
    }

    /**
     * Reset to maximum volume , then minus to minimum step by step.
     */
    @Override
    public void volumeResetAndFadeOut() {
        mmFadeHandler.removeCallbacksAndMessages(null);
        mmVolume = VOLUME_MAX;
        loopFadeOut();
    }

    private void loopFadeOut() {
        setPlayerVolume();
        Log.i(TAG, "loopFadeOut() - mmVolume : " + mmVolume);
        if (mmVolume <= VOLUME_MIN) {
            mmVolume = VOLUME_MIN;
            return;
        }

        //Calculator
        mmVolume = (float) (Math.round((mmVolume - STEP_VOLUME) * 100)) / 100;
        mmFadeHandler.removeCallbacksAndMessages(null);
        mmFadeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loopFadeOut();
            }
        }, SINGLE_FADE_PERIOD);
    }

    private void setPlayerVolume() {
        if (mmVolume >= VOLUME_MIN && mmVolume <= VOLUME_MAX) {
            if (mVolumeFadeListener != null) {
                mVolumeFadeListener.onVolumeFadeChanged(mmVolume, mmVolume);
            }
        }
    }

    @Override
    public void volumeFadeDestroy() {
        mmFadeHandler.removeCallbacksAndMessages(null);
    }
}
