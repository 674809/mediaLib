package com.egar.scanner.api.activity;

import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.egar.scanner.api.EgarApiScanner;
import com.egar.scanner.api.IApiScannerActions;
import com.egar.scanner.api.MediaScanRespFactory;

import java.util.List;
import java.util.Map;

import juns.lib.media.flags.MediaType;
import juns.lib.media.provider.IDataOpActions;
import juns.lib.media.scanner.IMediaScanListener;
import juns.lib.media.scanner.IMediaScanService;

/**
 * {@link EgarApiScanner} API activity
 *
 * @author Jun.Wang
 */
public abstract class ApiScannerFragActivity extends FragmentActivity implements IApiScannerActions {
    //TAG
    private static final String TAG = "ApiScannerFragActivity";

    private int mMediaType = MediaType.ALL;
    private IMediaScanListener mMediaScanListener;
    private EgarApiScanner mEgarApiScanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEgarApiScanner = new EgarApiScanner(this, this);
    }

    /**
     * {@link EgarApiScanner#bindScanService()}
     * & {@link EgarApiScanner#unbindScanService()}
     *
     * @param mediaType   {@link MediaType}
     * @param isConnected true- Execute BIND; false- Execute UNBIND.
     */
    protected void bindScanService(int mediaType, boolean isConnected) {
        mMediaType = mediaType;
        if (mEgarApiScanner != null) {
            if (isConnected) {
                mEgarApiScanner.bindScanService();
            } else {
                mEgarApiScanner.unbindScanService();
            }
        }
    }

    /**
     * {@link EgarApiScanner.IEgarApiScanListener#onMediaScanServiceConnected()}
     */
    @Override
    public void onMediaScanServiceConnected() {
        if (mEgarApiScanner != null) {
            mMediaScanListener = new MediaScanRespFactory(this).getRespCallback();
            addScanListener(mMediaType, true, getApplication().getPackageName(), mMediaScanListener);
        }
    }

    /**
     * {@link EgarApiScanner.IEgarApiScanListener#onMediaScanServiceDisconnected()}
     */
    @Override
    public void onMediaScanServiceDisconnected() {
        removeScanListener(getApplication().getPackageName(), mMediaScanListener);
    }

    /**
     * {@link IMediaScanService#addScanListener(int, boolean, String, IMediaScanListener)}
     */
    public void addScanListener(int type, boolean isRespDelta, String tag, IMediaScanListener l) {
        if (mEgarApiScanner != null) {
            mEgarApiScanner.addScanListener(type, isRespDelta, tag, l);
        }
    }

    /**
     * {@link IMediaScanService#removeScanListener(String, IMediaScanListener)}
     */
    public void removeScanListener(String tag, IMediaScanListener l) {
        if (mEgarApiScanner != null) {
            mEgarApiScanner.removeScanListener(tag, l);
        }
    }

    /**
     * {@link IMediaScanService#startScan()}
     */
    public void startScan() {
        if (mEgarApiScanner != null) {
            mEgarApiScanner.startScan();
        }
    }

    /**
     * {@link IMediaScanService#isScanning(int)}
     */
    public boolean isScanning(int type) {
        return mEgarApiScanner != null && mEgarApiScanner.isScanning(type);
    }

    /**
     * {@link IMediaScanService#getStorageDevices()}
     */
    public List getStorageDevices() {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getStorageDevices();
        }
        return null;
    }

    public String getDbPath(int type) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getDbPath(type);
        }
        return null;
    }

    /**
     * {@link IMediaScanListener#onRespScanState(int)}
     */
    @Override
    public void onRespScanState(int state) {
    }

    /**
     * {@link IMediaScanListener#onRespMountChange(List)}
     */
    @Override
    public void onRespMountChange(List listStorageDevices) {
    }

    /**
     * {@link IMediaScanListener#onRespDeltaMedias(List)}
     */
    @Override
    public void onRespDeltaMedias(List listMedias) {
    }

    /**
     * {@link IDataOpActions#getAllMedias(int, int, String[])}
     */
    public List getAllMedias(int type, int sortBy, String[] params) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getAllMedias(type, sortBy, params);
        }
        return null;
    }

    /**
     * {@link IDataOpActions#getMediasByColumns(int, Map, String)}
     */
    public List getMediasByColumns(int type, Map<String, String> whereColumns, String sortOrder) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getMediasByColumns(type, whereColumns, sortOrder);
        }
        return null;
    }

    /**
     * {@link IDataOpActions#getFilterFolders(int)}
     */
    public List getFilterFolders(int type) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getFilterFolders(type);
        }
        return null;
    }

    /**
     * {@link IDataOpActions#getFilterArtists(int)}
     */
    public List getFilterArtists(int type) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getFilterArtists(type);
        }
        return null;
    }

    /**
     * {@link IDataOpActions#getFilterAlbums(int)}
     */
    public List getFilterAlbums(int type) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getFilterAlbums(type);
        }
        return null;
    }

    /**
     * {@link IDataOpActions#getAllMediaSheets(int, int)}
     */
    public List getAllMediaSheets(int type, int id) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getAllMediaSheets(type, id);
        }
        return null;
    }

    /**
     * {@link IDataOpActions#getAllMediaSheetMapInfos(int, int)}
     */
    public List getAllMediaSheetMapInfos(int type, int sheetId) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getAllMediaSheetMapInfos(type, sheetId);
        }
        return null;
    }

    /**
     * {@link IDataOpActions#addMediaSheetMapInfos(int, List)}
     */
    public int addMediaSheetMapInfos(int type, List listMapInfos) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.addMediaSheetMapInfos(type, listMapInfos);
        }
        return 0;
    }

    /**
     * {@link IDataOpActions#deleteMediaSheetMapInfos(int, int)}
     */
    public int deleteMediaSheetMapInfos(int type, int sheetId) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.deleteMediaSheetMapInfos(type, sheetId);
        }
        return 0;
    }

    /**
     * {@link IDataOpActions#addMediaSheet(int, List)}
     */
    public int addMediaSheet(int type, List mediaSheet) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.addMediaSheet(type, mediaSheet);
        }
        return 0;
    }

    /**
     * {@link IMediaScanService#updateMediaCollect(int, List)}
     */
    public int updateMediaCollect(int type, List mediasToCollect) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.updateMediaCollect(type, mediasToCollect);
        }
        return 0;
    }

    /**
     * {@link IMediaScanService#clearHistoryCollect(int)}
     */
    public int clearHistoryCollect(int mediaType) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.clearHistoryCollect(mediaType);
        }
        return 0;
    }

    /**
     * {@link IDataOpActions#updateMediaSheet(int, List)}
     */
    public int updateMediaSheet(int type, List mediaSheet) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.updateMediaSheet(type, mediaSheet);
        }
        return 0;
    }

    /**
     * {@link IMediaScanService#getCountInDB(int)}
     */
    public long getCountInDB(int type) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getCountInDB(type);
        }
        return 0;
    }

    @Override
    public IBinder asBinder() {
        return null;
    }

    @Override
    public void finish() {
        clearActivity();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        clearActivity();
        super.onDestroy();
    }

    private void clearActivity() {
        removeScanListener(getApplication().getPackageName(), this);
    }
}
