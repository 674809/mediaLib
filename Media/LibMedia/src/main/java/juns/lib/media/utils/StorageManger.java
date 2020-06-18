package juns.lib.media.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.StorageDevice;

/**
 * 多SD卡Mount状态逻辑处理类
 *
 * <p>监听Mount消息，获取新插入的且状态为已挂载的</p>
 *
 * @author Jun.Wang
 */
public class StorageManger {
    //TAG
    private static final String TAG = "StorageManger";

    //
    private Context mContext;
    //
    private StorageManagerListener mStorageManagerListener;

    //
    private static final int FILTER_MOUNTED = 1, FILTER_MOUNTED_TEST = 2;
    private static final int FILTER_UN_MOUNTED = -1, FILTER_UN_MOUNTED_TEST = -2;

    public interface StorageManagerListener {
        void onRespMounted(List listStorageDevices);

        void onRespUMounted(List listStorageDevices);
    }

    public StorageManger(Context context, StorageManagerListener l) {
        mContext = context;
        mStorageManagerListener = l;
    }

    public void onStorageMounted(boolean isTest) {
        Logs.i(TAG, "onStorageMounted(isTest: " + isTest + ")");
        filter(isTest ? FILTER_MOUNTED_TEST : FILTER_MOUNTED);
    }

    public void onStorageUnmounted(boolean isTest) {
        Logs.i(TAG, "onStorageUnmounted(isTest: " + isTest + ")");
        filter(isTest ? FILTER_UN_MOUNTED_TEST : FILTER_UN_MOUNTED);
    }

    /**
     * Filter storage mount information.
     *
     * @param flag
     */
    private synchronized void filter(final int flag) {
        //
        List<StorageDevice> listStorageDevices = new ArrayList<>();
        Map<String, StorageDevice> mapStorage = SDCardUtils.getMapMountedStorage(mContext);
        if (!EmptyUtil.isEmpty(mapStorage)) {
            listStorageDevices.addAll(mapStorage.values());
        }
        switch (flag) {
            case FILTER_MOUNTED:
                Logs.i(TAG, "----FILTER_MOUNTED--");
                if (mStorageManagerListener != null) {
                    mStorageManagerListener.onRespMounted(listStorageDevices);
                }
                break;
            case FILTER_MOUNTED_TEST:
                Logs.i(TAG, "----FILTER_MOUNTED_TEST--");
                if (mStorageManagerListener != null) {
                    mStorageManagerListener.onRespMounted(listStorageDevices);
                }
                break;
            case FILTER_UN_MOUNTED:
                Logs.i(TAG, "----FILTER_UN_MOUNTED--");
                if (mStorageManagerListener != null) {
                    mStorageManagerListener.onRespUMounted(listStorageDevices);
                }
                break;
            case FILTER_UN_MOUNTED_TEST:
                Logs.i(TAG, "----FILTER_UN_MOUNTED_TEST--");
                if (mStorageManagerListener != null) {
                    mStorageManagerListener.onRespUMounted(listStorageDevices);
                }
                break;
        }
    }
}
