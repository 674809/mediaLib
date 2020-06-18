package com.egar.scanner.api;

import android.os.RemoteException;

import java.util.List;

import juns.lib.media.scanner.IMediaScanListener;

public class MediaScanRespFactory {
    private IMediaScanListener mSrcListener;
    private RemoteCallback mRemoteCallback;

    public MediaScanRespFactory(IMediaScanListener l) {
        mSrcListener = l;
        mRemoteCallback = new RemoteCallback();
    }

    public IMediaScanListener getRespCallback() {
        return mRemoteCallback;
    }

    private class RemoteCallback extends IMediaScanListener.Stub {
        @Override
        public void onRespScanState(int state) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onRespScanState(state);
            }
        }

        @Override
        public void onRespMountChange(List listStorageDevices) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onRespMountChange(listStorageDevices);
            }
        }

        @Override
        public void onRespDeltaMedias(List listMedias) throws RemoteException {
            if (mSrcListener != null) {
                mSrcListener.onRespDeltaMedias(listMedias);
            }
        }
    }
}
