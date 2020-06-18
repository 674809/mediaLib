package com.egar.music.api;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.egar.scanner.api.EgarApiProvider;

import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.flags.MediaType;
import juns.lib.media.play.IAudioPlayListener;
import juns.lib.media.play.IAudioPlayService;
import juns.lib.media.provider.audio.AudioProviderInfo;

/**
 * 音频播放API类
 *
 * @author Jun.Wang
 */
public class EgarApiMusic implements IAudioPlayService, IAudioDataOpActions {
    //TAG
    private static final String TAG = "EgarApiMusic";

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
    private IEgarApiMusicListener mEgarApiMusicListener;

    @Override
    public IBinder asBinder() {
        return null;
    }

    public interface IEgarApiMusicListener {
        void onAudioPlayServiceConnected();

        void onAudioPlayServiceDisconnected();
    }

    /**
     * 扫描服务句柄
     */
    private IAudioPlayService mAudioPlayService;
    private ServiceConnection mAudioPlayServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mEgarApiMusicListener != null) {
                mAudioPlayService = IAudioPlayService.Stub.asInterface(service);
                mEgarApiMusicListener.onAudioPlayServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mEgarApiMusicListener != null) {
                mAudioPlayService = null;
                mEgarApiMusicListener.onAudioPlayServiceDisconnected();
            }
        }
    };

    public EgarApiMusic(Context context, IEgarApiMusicListener l) {
        mEgarApiProvider = new EgarApiProvider(context);
        mContext = context;
        mEgarApiMusicListener = l;
    }

    public void bindPlayService() {
        try {
            if (mContext != null) {
                Intent intent = getPlayServiceIntent();
                mContext.startService(intent);
                mContext.bindService(intent, mAudioPlayServiceConnection, Context.BIND_AUTO_CREATE);
            }
        } catch (Exception e) {
            Logs.i(TAG, "bindPlayService() >> e: " + e.getMessage());
        }
    }

    public void unbindPlayService() {
        try {
            if (mContext != null) {
                mContext.unbindService(mAudioPlayServiceConnection);
            }
        } catch (Exception e) {
            Logs.i(TAG, "unbindPlayService() >> e: " + e.getMessage());
        }
    }

    private Intent getPlayServiceIntent() {
        String pkg = "com.egar.audio";
        String clsName = "com.egar.audio.service.AudioPlayService";
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(pkg, clsName));
        return intent;
    }

    public boolean isPlayServiceConnected() {
        return (mAudioPlayService != null);
    }

    @Override
    public void addPlayListener(boolean isRespDelta, String tag, IAudioPlayListener l) {
        if (mAudioPlayService != null) {
            try {
                Logs.i(TAG, "addPlayListener() - tag:" + tag);
                mAudioPlayService.addPlayListener(isRespDelta, tag, l);
            } catch (RemoteException e) {
                Logs.i(TAG, "addPlayListener() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removePlayListener(String tag, IAudioPlayListener l) {
        if (mAudioPlayService != null) {
            try {
                Logs.i(TAG, "removePlayListener() - tag:" + tag);
                mAudioPlayService.removePlayListener(tag, l);
            } catch (RemoteException e) {
                Logs.i(TAG, "removePlayListener() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void autoPlay() {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.autoPlay();
            } catch (RemoteException e) {
                Logs.i(TAG, "autoPlay() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isScanning() {
        try {
            return mAudioPlayService != null && mAudioPlayService.isScanning();
        } catch (RemoteException e) {
            Logs.i(TAG, "isScanning() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List getStorageDevices() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getStorageDevices();
            } catch (Exception e) {
                Logs.i(TAG, "getStorageDevices() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public long getCountInDB() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getCountInDB();
            } catch (Exception e) {
                Logs.i(TAG, "getCountInDB() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public List getAllMedias(int sortBy, String[] params) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getAllMedias(MediaType.AUDIO, sortBy, params);
        }
        return null;
    }

    /**
     * Get medias & sync play list.
     */
    public List getAndSyncAllMedias(int sortBy, String[] params) {
        // Get play list from MusicPlayService.
        List list = getAllMedias(sortBy, params);
        // Update play list of MusicPlayService.
        applyPlayList(null);
        return list;
    }

    @Override
    public List getMediasByColumns(Map<String, String> whereColumns, String sortOrder) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getMediasByColumns(MediaType.AUDIO, whereColumns, sortOrder);
        }
        return null;
    }

    public List getAndSyncMediasByColumns(Map<String, String> whereColumns, String sortOrder, String[] params) {
        List list = getMediasByColumns(whereColumns, null);
        applyPlayList(params);
        return list;
    }

    /**
     * 查询过滤条件 - 文件夹
     */
    @Override
    public List getFilterFolders() {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getFilterFolders(MediaType.AUDIO);
        }
        return null;
    }

    @Override
    public List getFilterArtists() {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getFilterArtists(MediaType.AUDIO);
        }
        return null;
    }

    @Override
    public List getFilterAlbums() {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getFilterAlbums(MediaType.AUDIO);
        }
        return null;
    }

    @Override
    public int updateMediaCollect(int position, ProAudio media) {
        int rowId = 0;
        if (mAudioPlayService != null) {
            try {
                rowId = mAudioPlayService.updateMediaCollect(position, media);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return rowId;
    }

    @Override
    public int clearHistoryCollect() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.clearHistoryCollect();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public List getAllMediaSheets(int id) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getAllMediaSheets(MediaType.AUDIO, id);
        }
        return null;
    }

    @Override
    public int addMediaSheet(List mediaSheet) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.addMediaSheet(MediaType.AUDIO, mediaSheet);
        }
        return 0;
    }

    @Override
    public int updateMediaSheet(List mediaSheet) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.updateMediaSheet(MediaType.AUDIO, mediaSheet);
        }
        return 0;
    }

    @Override
    public List getAllMediaSheetMapInfos(int sheetId) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.getAllMediaSheetMapInfos(MediaType.AUDIO, sheetId);
        }
        return null;
    }

    @Override
    public int addMediaSheetMapInfos(final List listMapInfos) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.addMediaSheetMapInfos(MediaType.AUDIO, listMapInfos);
        }
        return 0;
    }

    @Override
    public int deleteMediaSheetMapInfos(int sheetId) {
        if (mEgarApiProvider != null) {
            return mEgarApiProvider.deleteMediaSheetMapInfos(MediaType.AUDIO, sheetId);
        }
        return 0;
    }

    @Override
    public ProAudio getCurrMedia() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getCurrMedia();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getCurrMediaPath() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getCurrMediaPath();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public long getProgress() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getProgress();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getDuration();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        try {
            return mAudioPlayService != null && mAudioPlayService.isPlaying();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void play() {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.play();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void applyPlayList(String[] params) {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.applyPlayList(params);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void applyPlayInfo(String mediaUrl, int pos) {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.applyPlayInfo(mediaUrl, pos);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playByUrlByUser(String mediaPath) {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.playByUrlByUser(mediaPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playPrevByUser() {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.playPrevByUser();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playNextByUser() {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.playNextByUser();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playOrPauseByUser() {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.playOrPauseByUser();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void release() {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.release();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void seekTo(int time) {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.seekTo(time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isSeeking() {
        try {
            return mAudioPlayService != null && mAudioPlayService.isSeeking();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void switchPlayMode(int supportFlag) {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.switchPlayMode(supportFlag);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPlayMode(int mode) {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.setPlayMode(mode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getPlayMode() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getPlayMode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public String getLastMediaUrl() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getLastMediaUrl();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    @Override
    public long getLastProgress() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getLastProgress();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void focusPlayer() {
        if (mAudioPlayService != null) {
            try {
                mAudioPlayService.focusPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isPlayerFocused() {
        try {
            return mAudioPlayService != null && mAudioPlayService.isPlayerFocused();
        } catch (RemoteException e) {
            Log.i(TAG, "isPlayerFocused() >> e: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int getCurrPos() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getCurrPos();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int getTotalCount() {
        if (mAudioPlayService != null) {
            try {
                return mAudioPlayService.getTotalCount();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 被用户暂停
     *
     * @param context {@link Context}
     */
    public static void pausedByUser(Context context, boolean pausedByUser) {
        try {
            final String KEY = "isPausedByUser";
            ContentResolver cr = context.getContentResolver();
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY, pausedByUser);
            cr.call(AudioProviderInfo.getUriGetBool(), "setBool", KEY, bundle);
        } catch (Exception e) {
            Logs.i(TAG, "isPauseByUser() >> e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 是否被用户暂停？
     *
     * @param context {@link Context}
     * @return true:被用户暂停
     */
    public static boolean isPausedByUser(Context context) {
        try {
            final String KEY = "isPausedByUser";
            ContentResolver cr = context.getContentResolver();
            Bundle bundle = cr.call(AudioProviderInfo.getUriGetBool(), "getBool", KEY, null);
            if (bundle != null) {
                return bundle.getBoolean(KEY);
            }
        } catch (Exception e) {
            Logs.i(TAG, "isPauseByUser() >> e: " + e.getMessage());
        }
        return false;
    }

    /**
     * Media play enable state.
     *
     * @param context {@link Context}
     * @return true -Allow play.
     */
    public static boolean isPlayEnable(Context context) {
        return !isPausedByUser(context);
    }
}
