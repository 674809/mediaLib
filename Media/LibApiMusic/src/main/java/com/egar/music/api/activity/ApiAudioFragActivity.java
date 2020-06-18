package com.egar.music.api.activity;

import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.egar.music.api.AudioPlayRespFactory;
import com.egar.music.api.EgarApiMusic;
import com.egar.music.api.IApiAudioActions;
import com.egar.music.api.IAudioDataOpActions;

import java.util.List;
import java.util.Map;

import juns.lib.android.activity.BaseFragActivity;
import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.play.IAudioPlayListener;
import juns.lib.media.play.IAudioPlayService;

/**
 * Base fragment activity of API
 *
 * @author Jun.Wang
 */
public abstract class ApiAudioFragActivity extends BaseFragActivity implements IApiAudioActions {
    //TAG
    private static final String TAG = "ApiAudioFragActivity";

    //
    private EgarApiMusic mEgarApiMusic;
    private IAudioPlayListener mAudioPlayListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEgarApiMusic = new EgarApiMusic(this, this);
    }

    /**
     * {@link EgarApiMusic.IEgarApiMusicListener#onAudioPlayServiceConnected()}
     */
    @Override
    public void onAudioPlayServiceConnected() {
        if (mEgarApiMusic != null) {
            mAudioPlayListener = new AudioPlayRespFactory(this).getRespCallback();
            addPlayListener(true, getListenerTag(), mAudioPlayListener);
        }
    }

    /**
     * {@link EgarApiMusic.IEgarApiMusicListener#onAudioPlayServiceDisconnected()}
     */
    @Override
    public void onAudioPlayServiceDisconnected() {
        removePlayListener();
    }

    private void removePlayListener() {
        removePlayListener(getListenerTag(), mAudioPlayListener);
    }

    private String getListenerTag() {
        try {
            return getClass().getName();// e.g. com.egar.music.activity.MusicListActivity
        } catch (Exception e) {
            return "DEFAULT_MUSIC";
        }
    }

    /**
     * {@link EgarApiMusic.IEgarApiMusicListener#isPlayServiceConnected()}
     */
    public boolean isPlayServiceConnected() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.isPlayServiceConnected();
        }
        return false;
    }

    /**
     * {@link EgarApiMusic.IEgarApiMusicListener#bindPlayService()}
     * & {@link EgarApiMusic.IEgarApiMusicListener#unbindPlayService()}
     */
    protected void bindPlayService(boolean isConnected) {
        if (isConnected) {
            mEgarApiMusic.bindPlayService();
        } else {
            mEgarApiMusic.unbindPlayService();
        }
    }

    /**
     * {@link IAudioPlayService#addPlayListener(boolean, String, IAudioPlayListener)}
     */
    public void addPlayListener(boolean isRespDelta, String tag, IAudioPlayListener l) {
        if (mEgarApiMusic != null) {
            Logs.i(TAG, "addPlayListener(" + isRespDelta + "," + tag + "," + l + ")");
            mEgarApiMusic.addPlayListener(isRespDelta, tag, l);
        }
    }

    /**
     * {@link IAudioPlayService#removePlayListener(String, IAudioPlayListener)}
     */
    public void removePlayListener(String tag, IAudioPlayListener l) {
        if (mEgarApiMusic != null) {
            Logs.i(TAG, "removePlayListener(" + tag + ")");
            mEgarApiMusic.removePlayListener(tag, l);
        }
    }

    public void autoPlay() {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.autoPlay();
        }
    }

    /**
     * {@link IAudioPlayService#isScanning()}
     */
    public boolean isScanning() {
        return mEgarApiMusic != null && mEgarApiMusic.isScanning();
    }

    /**
     * {@link IAudioPlayService#getStorageDevices()}
     */
    public List getStorageDevices() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getStorageDevices();
        }
        return null;
    }

    /**
     * {@link IAudioPlayService#getCurrPos()}
     * 获取当前媒体在播放列表中的位置
     */
    public int getCurrPos() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getCurrPos();
        }
        return 0;
    }

    /**
     * {@link IAudioPlayService#getTotalCount()}
     *  获取播放列表总数
     */
    public int getTotalCount() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getTotalCount();
        }
        return 0;
    }

    /**
     * {@link IAudioPlayService#getCurrMedia()}
     * // 获取当前媒体媒体对象
     */
    public ProAudio getCurrMedia() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getCurrMedia();
        }
        return null;
    }

    /**
     * {@link IAudioPlayService#getCurrMediaPath()}
     */
    public String getCurrMediaPath() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getCurrMediaPath();
        }
        return null;
    }

    /**
     * {@link IAudioPlayService#getProgress()}
     */
    public long getProgress() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getProgress();
        }
        return 0;
    }

    /**
     * {@link IAudioPlayService#getDuration()}
     */
    public long getDuration() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getDuration();
        }
        return 0;
    }

    /**
     * {@link IAudioPlayService#isPlaying()}
     */
    public boolean isPlaying() {
        return mEgarApiMusic != null && mEgarApiMusic.isPlaying();
    }

    /**
     * {@link IAudioPlayService#play()}
     */
    public void play() {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.play();
        }
    }

    /**
     * 通知音频服务更新播放列表
     * @param params
     */
    public void applyPlayList(String[] params) {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.applyPlayList(params);
        }
    }

    /**
     *通知音频服务更新播放信息
     */
    public void applyPlayInfo(String mediaUrl, int pos) {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.applyPlayInfo(mediaUrl, pos);
        }
    }

    /**
     * {@link IAudioPlayService#playByUrlByUser(String)}
     * 播放指定某一条歌曲
     */
    public void playByUrlByUser(String mediaPath) {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.playByUrlByUser(mediaPath);
        }
    }

    /**
     * {@link IAudioPlayService#playPrevByUser()}
     * 播放上一曲
     */
    public void playPrevByUser() {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.playPrevByUser();
        }
    }

    /**
     * {@link IAudioPlayService#playNextByUser()}
     * 播放下一首
     */
    public void playNextByUser() {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.playNextByUser();
        }
    }

    /**
     * {@link IAudioPlayService#playOrPauseByUser()}
     */
    public void playOrPauseByUser() {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.playOrPauseByUser();
        }
    }

    /**
     * {@link IAudioPlayService#release()}
     */
    public void release() {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.release();
        }
    }

    /**
     * {@link IAudioPlayService#seekTo(int)}
     */
    public void seekTo(int time) {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.seekTo(time);
        }
    }

    /**
     * {@link IAudioPlayService#isSeeking()}
     */
    public boolean isSeeking() {
        return mEgarApiMusic != null && mEgarApiMusic.isSeeking();
    }

    /**
     * {@link IAudioPlayService#getLastMediaUrl()}
     */
    public String getLastMediaUrl() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getLastMediaUrl();
        }
        return "";
    }

    /**
     * {@link IAudioPlayService#getLastProgress()}
     */
    public long getLastProgress() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getLastProgress();
        }
        return 0;
    }

    /**
     * {@link IAudioPlayService#switchPlayMode(int)}
     */
    public void switchPlayMode(int supportFlag) {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.switchPlayMode(supportFlag);
        }
    }

    /**
     * {@link IAudioPlayService#setPlayMode(int)}
     */
    public void setPlayMode(int mode) {
        if (mEgarApiMusic != null) {
            mEgarApiMusic.setPlayMode(mode);
        }
    }

    /**
     * {@link IAudioPlayService#getPlayMode()}
     */
    public int getPlayMode() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getPlayMode();
        }
        return 0;
    }

    /**
     * {@link IAudioPlayService#focusPlayer()}
     */
    public void focusPlayer() {
        if (mEgarApiMusic != null) {
            try {
                mEgarApiMusic.focusPlayer();
            } catch (Exception e) {
                Logs.i(TAG, "focusPlayer() >> e: " + e.getMessage());
            }
        }
    }

    /**
     * {@link IAudioPlayService#isPlayerFocused()}
     */
    public boolean isPlayerFocused() {
        try {
            return mEgarApiMusic != null && mEgarApiMusic.isPlayerFocused();
        } catch (Exception e) {
            Logs.i(TAG, "isPlayerFocused() >> e: " + e.getMessage());
            return false;
        }
    }

    /**
     * {@link IAudioPlayService#updateMediaCollect(int, ProAudio)}
     */
    public int updateMediaCollect(int position, ProAudio media) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.updateMediaCollect(position, media);
        }
        return 0;
    }

    /**
     * {@link IAudioPlayListener#onMountStateChanged(List)}
     */
    @Override
    public void onMountStateChanged(List listStorageDevices) {
    }

    /**
     * {@link IAudioPlayListener#onScanStateChanged(int)}
     */
    @Override
    public void onScanStateChanged(int state) {
    }

    /**
     * {@link IAudioPlayListener#onGotDeltaMedias(List)}
     */
    @Override
    public void onGotDeltaMedias(List listMedias) {
    }

    /**
     * {@link IAudioPlayListener#onPlayStateChanged(int)}
     */
    @Override
    public void onPlayStateChanged(int playStateValue) {
    }

    /**
     * {@link IAudioPlayListener#onPlayProgressChanged(String, int, int)}
     */
    @Override
    public void onPlayProgressChanged(String mediaPath, int progress, int duration) {
    }

    /**
     * {@link IAudioPlayListener#onPlayModeChanged(int)}
     */
    @Override
    public void onPlayModeChanged(int newPlayMode) {
    }

    /**
     * {@link IAudioDataOpActions#getAllMedias(int, String[])}
     */
    public List getAllMedias(int sortBy, String[] params) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getAllMedias(sortBy, params);
        }
        return null;
    }

    public List getAndSyncAllMedias(int sortBy, String[] params) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getAndSyncAllMedias(sortBy, params);
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#getMediasByColumns(Map, String)}
     */
    public List getMediasByColumns(Map<String, String> whereColumns, String sortOrder) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getMediasByColumns(whereColumns, sortOrder);
        }
        return null;
    }

    public List getAndSyncMediasByColumns(Map<String, String> whereColumns, String sortOrder, String[] params) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getAndSyncMediasByColumns(whereColumns, sortOrder, params);
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#getFilterFolders()}
     */
    public List getFilterFolders() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getFilterFolders();
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#getFilterArtists()}
     */
    public List getFilterArtists() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getFilterArtists();
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#getFilterAlbums()}
     */
    public List getFilterAlbums() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getFilterAlbums();
        }
        return null;
    }

    /**
     * {@link IAudioPlayService#clearHistoryCollect()}
     */
    public int clearHistoryCollect() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.clearHistoryCollect();
        }
        return 0;
    }

    /**
     * {@link IAudioDataOpActions#getAllMediaSheets(int)}
     */
    public List getAllMediaSheets(int id) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getAllMediaSheets(id);
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#addMediaSheet(List)}
     */
    public int addMediaSheet(List mediaSheet) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.addMediaSheet(mediaSheet);
        }
        return 0;
    }

    /**
     * {@link IAudioDataOpActions#updateMediaSheet(List)}
     */
    public int updateMediaSheet(List mediaSheet) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.updateMediaSheet(mediaSheet);
        }
        return 0;
    }

    /**
     * {@link IAudioDataOpActions#getAllMediaSheetMapInfos(int)}
     */
    public List getAllMediaSheetMapInfos(int sheetId) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getAllMediaSheetMapInfos(sheetId);
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#addMediaSheetMapInfos(List)}
     */
    public int addMediaSheetMapInfos(List listMapInfos) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.addMediaSheetMapInfos(listMapInfos);
        }
        return 0;
    }

    /**
     * {@link IAudioDataOpActions#deleteMediaSheetMapInfos(int)}
     */
    public int deleteMediaSheetMapInfos(int sheetId) {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.deleteMediaSheetMapInfos(sheetId);
        }
        return 0;
    }

    /**
     * {@link IAudioPlayService#getCountInDB()}
     */
    public long getCountInDB() {
        if (mEgarApiMusic != null) {
            return mEgarApiMusic.getCountInDB();
        }
        return 0;
    }

    @Override
    public IBinder asBinder() {
        return null;
    }

    @Override
    public void finish() {
        super.finish();
        Logs.i(TAG, "finish()");
        clearActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.i(TAG, "onDestroy()");
        clearActivity();
    }

    private void clearActivity() {
        Logs.i(TAG, "clearActivity()");
        removePlayListener();
    }
}
