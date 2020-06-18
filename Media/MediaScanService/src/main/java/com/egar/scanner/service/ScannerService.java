package com.egar.scanner.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.egar.scanner.service.presenter.IScanPresenter;
import com.egar.scanner.service.presenter.ScanPresenter;

import java.util.List;

import juns.lib.android.utils.Logs;
import juns.lib.media.action.MediaActions;
import juns.lib.media.utils.StorageManger;

/**
 * Scanner service
 * <p>1. Process scanner logic</p>
 * <p>2. Process logic from {@link com.egar.scanner.receiver.MediaScanReceiver}</p>
 *
 * @author Jun.Wang
 */
public class ScannerService extends Service {
    //TAG
    private static final String TAG = "ScannerService";

    //Param
    public static final String PARAM_ACTION = "ACTION";

    /**
     * {@link IScanPresenter}
     */
    private IScanPresenter mScanPresenter;

    private IScanPresenter getScanPresenter() {
        if (mScanPresenter == null) {
            mScanPresenter = new ScanPresenter(this);
        }
        return mScanPresenter;
    }

    /**
     * {@link StorageManger}
     */
    private StorageManger mStorageManger;

    private StorageManger getStorageManger() {
        if (mStorageManger == null) {
            mStorageManger = new StorageManger(this, new StorageManger.StorageManagerListener() {
                @Override
                public void onRespMounted(List listStorageDevices) {
                    getScanPresenter().onRespMounted(listStorageDevices);
                }

                @Override
                public void onRespUMounted(List listStorageDevices) {
                    getScanPresenter().onRespUMounted(listStorageDevices);
                }
            });
        }
        return mStorageManger;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.i(TAG, "onCreate()");
        keepAlive();
    }

    private void keepAlive() {
        Logs.i(TAG, "keepAlive()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return getScanPresenter().asBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.i(TAG, "onStartCommand()");
        if (intent != null) {
            String action = intent.getStringExtra(PARAM_ACTION);
            intent.removeExtra(PARAM_ACTION);
            Logs.i(TAG, "action : " + action);
            if (action != null) {
                switch (action) {
                    case MediaActions.STORAGE_TEST_MOUNTED:
                        getStorageManger().onStorageMounted(true);
                        break;
                    case MediaActions.STORAGE_MOUNTED:
                        getStorageManger().onStorageMounted(false);
                        break;
                    case MediaActions.STORAGE_TEST_UN_MOUNTED:
                        getStorageManger().onStorageUnmounted(true);
                        break;
                    case MediaActions.STORAGE_EJECT:
                        getStorageManger().onStorageUnmounted(false);
                        break;
                    //case MediaActions.STORAGE_UNMOUNTED:
                    //    getStorageManger().onStorageUmounted(false);
                    //    break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logs.i(TAG, "onDestroy()");
        if (mScanPresenter != null) {
            ((ScanPresenter) mScanPresenter).destroy();
            mScanPresenter = null;
        }
        if (mStorageManger != null) {
            mStorageManger = null;
        }
        super.onDestroy();
    }
}
