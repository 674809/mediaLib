package com.egar.scanner.service.presenter;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.MediaFile;
import juns.lib.media.flags.MediaType;
import juns.lib.media.scanner.IMediaScanListener;
import juns.lib.media.utils.ScannerFileUtils;

/**
 * Scanner presenter base.
 *
 * @author Jun.Wang
 */
public abstract class ScanPresenterBase extends IScanPresenter {
    //TAG
    private static final String TAG = "ScanPresenterBase";

    /**
     * 扫描 - 监听回调
     */
    private List<MediaScanResp> mListScanListeners = new ArrayList<>();

    public ScanPresenterBase(Context context) {
    }

    @Override
    public void addScanListener(int type, boolean isRespDelta, String tag, IMediaScanListener l) {
        if (l != null) {
            try {
                Log.i(TAG, "addScanListener(" + type + "," + isRespDelta + "," + tag + "," + l.toString() + ")");
                // Link
                final MediaScanResp respL = new MediaScanResp(type, isRespDelta, tag, l);
                respL.asBinder().linkToDeath((respL.mmDeathRecipient = new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        Logs.i(TAG, "addScanListener() >> binderDied()");
                        mListScanListeners.remove(respL);
                    }
                }), 0);
                // Add to list.
                mListScanListeners.add(respL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeScanListener(String tag, IMediaScanListener l) {
        if (l != null) {
            Logs.i(TAG, "removeScanListener(" + tag + "," + l.toString() + ")");
            for (MediaScanResp respL : mListScanListeners) {
                try {
                    if (respL.mmMediaScanListener == l) {
                        // Unlink
                        respL.asBinder().unlinkToDeath(respL.mmDeathRecipient, 0);
                        // Remove from list.
                        mListScanListeners.remove(respL);
                        break;
                    }
                } catch (Exception e) {
                    Logs.i(TAG, "");
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void startScan() throws RemoteException {
    }

    @Override
    public boolean isScanning(int type) throws RemoteException {
        return false;
    }

    @Override
    public int updateMediaCollect(int type, List mediasToCollect) throws RemoteException {
        return 0;
    }

    @Override
    public int clearHistoryCollect(int mediaType) throws RemoteException {
        return 0;
    }

    @Override
    public long getCountInDB(int type) throws RemoteException {
        return 0;
    }

    @Override
    public List getStorageDevices() throws RemoteException {
        return null;
    }

    @Override
    public String getDbPath(int type) {
        return ScannerFileUtils.getDbFilePath(type);
    }

    @Override
    public void onRespMounted(List listStorageDevices) {
        Logs.i(TAG, "onRespMounted(" + (listStorageDevices == null ? 0 : listStorageDevices.size()) + ")");
        notifyMountChange(listStorageDevices);
    }

    @Override
    public void onRespUMounted(List listStorageDevices) {
        Logs.i(TAG, "onRespUMounted(" + (listStorageDevices == null ? 0 : listStorageDevices.size()) + ")");
        notifyMountChange(listStorageDevices);
    }

    private final class MediaScanResp {
        int mmType;
        boolean mmIsRespDelta;
        String mmTag;
        IMediaScanListener mmMediaScanListener;
        DeathRecipient mmDeathRecipient;

        MediaScanResp(int type, boolean isRespDelta, String tag, IMediaScanListener l) {
            mmType = type;
            mmIsRespDelta = isRespDelta;
            mmTag = tag;
            mmMediaScanListener = l;
        }

        IBinder asBinder() {
            return mmMediaScanListener.asBinder();
        }
    }

    /**
     * Notify scanning state
     *
     * @param mediaType {@link MediaType}
     * @param scanState {@link juns.lib.media.flags.MediaScanState}
     */
    void notifyScanState(int mediaType, int scanState) {
        for (MediaScanResp respL : mListScanListeners) {
            if (respL != null) {
                //Logs.i(TAG, "respL -> {l:" + respL.mmTag + ", lType/mediaType:" + respL.mmType + "/" + mediaType + ", scanState:" + scanState + "}");
                try {
                    // mediaType == MediaType.ALL 表示这个消息应该上报给所有的监听器
                    // respL.mmType == MediaType.ALL 表示这个监听器接收所有的消息
                    if (mediaType == MediaType.ALL
                            || respL.mmType == MediaType.ALL) {
                        respL.mmMediaScanListener.onRespScanState(scanState);

                        // 只有[监听器类型]和[消息类型]一致时才可以接收消息
                    } else if (respL.mmType == mediaType) {
                        respL.mmMediaScanListener.onRespScanState(scanState);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Notify delta data.
     *
     * @param type       {@link MediaType}
     * @param listMedias 增量列表
     */
    void notifyDeltaMedias(int type, List listMedias) {
        Logs.debugI(TAG, "notifyDeltaMedias(" + type + "," + (listMedias == null ? 0 : listMedias.size()) + ")" +
                "\n    respLs -> count:" + mListScanListeners.size());
        for (MediaScanResp respL : mListScanListeners) {
            if (respL != null) {
                try {
                    Logs.debugI(TAG, "respL -> [" + respL.mmTag + ";" + respL.mmType + ";" + respL.mmIsRespDelta + ";" + respL.mmMediaScanListener);
                    if ((respL.mmType == MediaType.ALL || respL.mmType == type) // 类型匹配
                            && respL.mmIsRespDelta) { // 需要增量数据
                        respL.mmMediaScanListener.onRespDeltaMedias(listMedias);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Notify storage device list that mount state changed.
     *
     * @param listStorageDevices list that mount state changed.
     */
    private void notifyMountChange(List listStorageDevices) {
        for (MediaScanResp respL : mListScanListeners) {
            if (respL != null) {
                try {
                    respL.mmMediaScanListener.onRespMountChange(listStorageDevices);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void destroy() {
        mListScanListeners.clear();
    }

    abstract class BaseRunnable implements Runnable {
        /**
         * 判断线程中业务逻辑是否正在运行中
         */
        boolean isProcessing = false;

        /**
         * 是否强制打断业务逻辑
         */
        boolean isForceBreak = false;

        void setProcessing(boolean processing) {
            isProcessing = processing;
        }

        boolean isProcessing() {
            return isProcessing;
        }

        boolean isForceBreak() {
            return isForceBreak;
        }

        public void setForceBreak(boolean forceBreak) {
            isForceBreak = forceBreak;
        }

        abstract void execProcessing() throws Exception;
    }

    abstract class BaseParseRunnable<T> extends BaseRunnable {
        /**
         * Parse first media of delta files, then remove first file from delta files.
         */
        abstract void parseFirstMedia();

        /**
         * Parse media information from file, then add to delta medias.
         *
         * @param mediaFile File parcel class.
         */
        abstract void parseMedia(MediaFile mediaFile);

        /**
         * Cache and notify delta medias
         *
         * @param isForce true-表示强制更新，即不管目前有多少个媒体，一律更新到数据库; false-表示按照一定的规律更新，如每5个
         * @param media   要缓存的媒体对象
         */
        abstract void cacheMedias(boolean isForce, T media);
    }
}
