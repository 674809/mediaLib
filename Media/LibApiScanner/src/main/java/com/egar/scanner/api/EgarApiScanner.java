package com.egar.scanner.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.media.provider.IDataOpActions;
import juns.lib.media.scanner.IMediaScanListener;
import juns.lib.media.scanner.IMediaScanService;

/**
 * 媒体扫描API类
 *
 * @author Jun.Wang
 */
public class EgarApiScanner implements IMediaScanService, IDataOpActions {
    //TAG
    private static final String TAG = "EgarApiScanner";

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * Provider API object.
     */
    private EgarApiProvider mEgarApiProvider;

    /**
     * API监听器，继承了IMediaScanListener
     */
    private IEgarApiScanListener mEgarApiScanListener;

    /**
     * Egar api scanner listener
     */
    public interface IEgarApiScanListener {
        void onMediaScanServiceConnected();

        void onMediaScanServiceDisconnected();
    }

    /**
     * 扫描服务句柄
     */
    private IMediaScanService mMediaScanService;
    private ServiceConnection mMediaScanServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logs.debugI(TAG, "mMediaScanServiceConnection - onServiceConnected");
            if (mEgarApiScanListener != null) {
                mMediaScanService = IMediaScanService.Stub.asInterface(service);
                mEgarApiScanListener.onMediaScanServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logs.debugI(TAG, "mMediaScanServiceConnection - onServiceDisconnected");
            if (mEgarApiScanListener != null) {
                mMediaScanService = null;
                mEgarApiScanListener.onMediaScanServiceDisconnected();
            }
        }
    };

    public EgarApiScanner(Context context, IEgarApiScanListener l) {
        mEgarApiProvider = new EgarApiProvider(context);
        mContext = context;
        mEgarApiScanListener = l;
    }

    public void bindScanService() {
        try {
            Intent intent = getScannerServiceIntent();
            mContext.startService(intent);
            mContext.bindService(intent, mMediaScanServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.i(TAG, "bindScanService() :: " + e.getMessage());
        }
    }

    public void unbindScanService() {
        try {
            mContext.unbindService(mMediaScanServiceConnection);
        } catch (Exception e) {
            Log.i(TAG, "unbindScanService() :: " + e.getMessage());
        }
    }

    private Intent getScannerServiceIntent() {
        String pkg = "com.egar.scanner";
        String clsName = "com.egar.scanner.service.ScannerService";
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(pkg, clsName));
        return intent;
    }

    public boolean isScanServiceConnected() {
        return (mMediaScanService != null);
    }

    @Override
    public void addScanListener(int type, boolean isRespDelta, String tag, IMediaScanListener l) {
        if (mMediaScanService != null) {
            try {
                mMediaScanService.addScanListener(type, isRespDelta, tag, l);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeScanListener(String tag, IMediaScanListener l) {
        if (mMediaScanService != null) {
            try {
                mMediaScanService.removeScanListener(tag, l);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startScan() {
        if (mMediaScanService != null) {
            try {
                mMediaScanService.startScan();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isScanning(int type) {
        try {
            return mMediaScanService != null && mMediaScanService.isScanning(type);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long getCountInDB(int type) {
        if (mMediaScanService != null) {
            try {
                return mMediaScanService.getCountInDB(type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public String getDbPath(int type) {
        if (mMediaScanService != null) {
            try {
                return mMediaScanService.getDbPath(type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List getStorageDevices() {
        if (mMediaScanService != null) {
            try {
                return mMediaScanService.getStorageDevices();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List getAllMedias(int type, int sortBy, String[] params) {
        Logs.i(TAG, "getAllMedias() : >> [mEgarApiProvider:" + mEgarApiProvider + "]");
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getAllMedias(type, sortBy, params);
        }
        return null;
    }

    @Override
    public List getMediasByColumns(int type, Map<String, String> whereColumns, String sortOrder) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getMediasByColumns(type, whereColumns, sortOrder);
        }
        return null;
    }

    @Override
    public List getFilterFolders(int type) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getFilterFolders(type);
        }
        return null;
    }

    @Override
    public List getFilterArtists(int type) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getFilterArtists(type);
        }
        return null;
    }

    @Override
    public List getFilterAlbums(int type) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getFilterAlbums(type);
        }
        return null;
    }

    /**
     * Update media favorite state.
     * <p>Deprecated method, please replace with {@link #updateMediaFavorite(int, List)}</p>
     *
     * @param type            {@link juns.lib.media.flags.MediaType}
     * @param mediasToCollect Media list.
     * @return ">0" means successful.
     */
    @Deprecated
    @Override
    public int updateMediaCollect(int type, List mediasToCollect) {
        int rowId = 0;
        if (mMediaScanService != null) {
            try {
                rowId = mMediaScanService.updateMediaCollect(type, mediasToCollect);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return rowId;
    }

    /**
     * Update media favorite state.
     *
     * @param type          {@link juns.lib.media.flags.MediaType}
     * @param mediasToFavor Media list to favor.
     * @return ">0" means successful.
     */
    @SuppressWarnings("deprecation")
    public int updateMediaFavorite(int type, List mediasToFavor) {
        return updateMediaCollect(type, mediasToFavor);
    }

    @Override
    public int clearHistoryCollect(int type) {
        if (mMediaScanService != null) {
            try {
                return mMediaScanService.clearHistoryCollect(type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public List getAllMediaSheets(int type, int id) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getAllMediaSheets(type, id);
        }
        return null;
    }

    @Override
    public int addMediaSheet(int type, List mediaSheet) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.addMediaSheet(type, mediaSheet);
        }
        return 0;
    }

    @Override
    public int updateMediaSheet(int type, List mediaSheet) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.updateMediaSheet(type, mediaSheet);
        }
        return 0;
    }

    @Override
    public List getAllMediaSheetMapInfos(int type, int sheetId) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getAllMediaSheetMapInfos(type, sheetId);
        }
        return null;
    }

    @Override
    public int addMediaSheetMapInfos(int type, final List listMapInfos) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.addMediaSheetMapInfos(type, listMapInfos);
        }
        return 0;
    }

    @Override
    public int deleteMediaSheetMapInfos(int type, int sheetId) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.deleteMediaSheetMapInfos(type, sheetId);
        }
        return 0;
    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}