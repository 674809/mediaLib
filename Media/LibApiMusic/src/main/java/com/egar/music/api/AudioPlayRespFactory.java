package com.egar.music.api;

import android.os.RemoteException;

import java.util.List;

import juns.lib.media.play.IAudioPlayListener;

public class AudioPlayRespFactory {

    private IAudioPlayListener mSrcListener;
    private IAudioPlayListener mRemoteCallback;

    public AudioPlayRespFactory(IAudioPlayListener l) {
        mSrcListener = l;
        mRemoteCallback = new RemoteCallback();
    }

    public IAudioPlayListener getRespCallback() {
        return mRemoteCallback;
    }

    private class RemoteCallback extends IAudioPlayListener.Stub {

        @Override
        public void onMountStateChanged(List listStorageDevices) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onMountStateChanged(listStorageDevices);
            }
        }

        @Override
        public void onScanStateChanged(int state) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onScanStateChanged(state);
            }
        }

        @Override
        public void onGotDeltaMedias(List listMedias) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onGotDeltaMedias(listMedias);
            }
        }

        @Override
        public void onPlayStateChanged(int playStateValue) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onPlayStateChanged(playStateValue);
            }
        }

        @Override
        public void onPlayProgressChanged(String mediaPath, int progress, int duration) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onPlayProgressChanged(mediaPath, progress, duration);
            }
        }

        @Override
        public void onPlayModeChanged(int newPlayMode) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onPlayModeChanged(newPlayMode);
            }
        }
    }
}
