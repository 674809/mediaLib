package com.egar.audio.service.presenter.impl;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.egar.audio.receiver.MediaBtnReceiver;
import com.egar.audio.service.presenter.IAudioFocusPresenter;
import com.egar.audio.service.presenter.IPlayPresenter;
import com.egar.audio.service.presenter.IVolumeFadePresenter;
import com.egar.audio.utils.AudioPreferUtils;
import com.egar.music.api.IAudioDataOpActions;
import com.egar.scanner.api.EgarApiScanner;
import com.egar.scanner.api.MediaScanRespFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.flags.MediaType;
import juns.lib.media.flags.PlayMode;
import juns.lib.media.flags.PlayModeSupportType;
import juns.lib.media.play.IAudioPlayListener;
import juns.lib.media.play.IAudioPlayService;
import juns.lib.media.scanner.IMediaScanListener;

/**
 * Audio play presenter base class
 *
 * @author Jun.Wang
 */
public class PlayPresenterBase extends IPlayPresenter {
    //TAG
    private static final String TAG = "PlayPresenterBase";

    /**
     * {@link Context}
     */
    private Context mContext;

    /**
     * 音频播放模式
     * {@link PlayMode}
     */
    private int mPlayMode;

    /**
     * 播放/扫描 - 监听回调
     */
    private List<AudioPlayResp> mListPlayListeners = new ArrayList<>();

    /**
     * {@link IVolumeFadePresenter} Object.
     */
    private IVolumeFadePresenter mVolumeFadePresenter;

    /**
     * Audio focus presenter implement.
     */
    private IAudioFocusPresenter mAudioFocusPresenter;

    /**
     * MediaScanService API
     */
    private EgarApiScanner mEgarApiScanner;
    private IMediaScanListener mMediaScanListener;

    public PlayPresenterBase(Context context) {
        mContext = context;
        // Initial VolumeFadePresenter
        mVolumeFadePresenter = new VolumeFadePresenter(this);

        // Initial AudioFocusPresenter
        mAudioFocusPresenter = new AudioFocusPresenter(context);
        mAudioFocusPresenter.setAudioFocusListener(this);

        // Initial EgarApiScanner
        mEgarApiScanner = new EgarApiScanner(context, this);
        bindScanService(true);
    }

    @Override
    public long getVolumeFadePeriod() {
        if (mVolumeFadePresenter != null) {
            return mVolumeFadePresenter.getVolumeFadePeriod();
        }
        return 0;
    }

    @Override
    public void volumeResetAndFadeIn() {
        if (mVolumeFadePresenter != null) {
            mVolumeFadePresenter.volumeResetAndFadeIn();
        }
    }

    @Override
    public void volumeResetAndFadeOut() {
        if (mVolumeFadePresenter != null) {
            mVolumeFadePresenter.volumeResetAndFadeOut();
        }
    }

    @Override
    public void volumeFadeDestroy() {
        if (mVolumeFadePresenter != null) {
            mVolumeFadePresenter.volumeFadeDestroy();
        }
    }

    @Override
    public void onVolumeFadeChanged(float leftVolume, float rightVolume) {
    }

    @Override
    public void onAudioFocusDuck() {
    }

    @Override
    public void onAudioFocusTransient() {
    }

    @Override
    public void onAudioFocusGain() {
    }

    @Override
    public void onAudioFocusLoss() {
    }

    private void bindScanService(boolean isBind) {
        Logs.i(TAG, "bindScanService(" + isBind + ")");
        try {
            if (mEgarApiScanner != null) {
                if (isBind) {
                    mEgarApiScanner.bindScanService();
                } else {
                    mEgarApiScanner.unbindScanService();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@link EgarApiScanner.IEgarApiScanListener#onMediaScanServiceConnected()}
     */
    @Override
    public void onMediaScanServiceConnected() {
        Logs.i(TAG, "onMediaScanServiceConnected()");
        if (mEgarApiScanner != null) {
            mMediaScanListener = new MediaScanRespFactory(this).getRespCallback();
            mEgarApiScanner.addScanListener(MediaType.AUDIO, true, mContext.getPackageName(), mMediaScanListener);
            startScan();
        }
    }

    /**
     * {@link EgarApiScanner.IEgarApiScanListener#onMediaScanServiceDisconnected()}
     */
    @Override
    public void onMediaScanServiceDisconnected() {
        Logs.i(TAG, "onMediaScanServiceDisconnected()");
        if (mEgarApiScanner != null) {
            mEgarApiScanner.removeScanListener(mContext.getPackageName(), mMediaScanListener);
        }
    }

    /**
     * {@link juns.lib.media.scanner.IMediaScanListener#onRespMountChange(List)}
     */
    @Override
    public void onRespMountChange(List listStorageDevices) {
        onMountStateChanged(listStorageDevices);
    }

    /**
     * {@link juns.lib.media.scanner.IMediaScanListener#onRespScanState(int)}
     */
    @Override
    public void onRespScanState(int state) throws RemoteException {
        onScanStateChanged(state);
    }

    /**
     * {@link juns.lib.media.scanner.IMediaScanListener#onRespDeltaMedias(List)}
     */
    @Override
    public void onRespDeltaMedias(List listMedias) throws RemoteException {
        Logs.debugI(TAG, "onRespDeltaMedias(" + (listMedias == null ? 0 : listMedias.size()) + ")");
        onGotDeltaMedias(listMedias);
    }

    /**
     * {@link com.egar.music.api.EgarApiMusic.IEgarApiMusicListener#addPlayListener(boolean, String, IAudioPlayListener)}
     */
    @Override
    public void addPlayListener(boolean isRespDelta, String tag, IAudioPlayListener l) {
        if (l != null) {
            try {
                Logs.i(TAG, "addPlayListener(" + isRespDelta + "," + tag + "," + l.toString() + ")");
                // Link
                final AudioPlayResp respL = new AudioPlayResp(isRespDelta, tag, l);
                respL.asBinder().linkToDeath((respL.mmDeathRecipient = new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        Logs.i(TAG, "addPlayListener() >> binderDied()");
                        mListPlayListeners.remove(respL);
                    }
                }), 0);
                // Add to list.
                mListPlayListeners.add(respL);
            } catch (RemoteException e) {
                Logs.i(TAG, "addPlayListener() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * {@link com.egar.music.api.EgarApiMusic.IEgarApiMusicListener#removePlayListener(IAudioPlayListener)}
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public void removePlayListener(String tag, IAudioPlayListener l) {
        if (l != null) {
            try {
                Logs.i(TAG, "removePlayListener(" + tag + "," + l.toString() + ")");
                for (AudioPlayResp respL : mListPlayListeners) {
                    if (respL.mmAudioPlayListener == l) {
                        // Unlink
                        respL.asBinder().unlinkToDeath(respL.mmDeathRecipient, 0);
                        // Remove from list.
                        mListPlayListeners.remove(respL);
                        break;
                    }
                }
            } catch (Exception e) {
                Logs.i(TAG, "removePlayListener() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void autoPlay() {
    }

    @Override
    public boolean isScanServiceConnected() {
        return mEgarApiScanner != null && mEgarApiScanner.isScanServiceConnected();
    }

    @Override
    public void startScan() {
        try {
            mEgarApiScanner.startScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isScanning() throws RemoteException {
        return mEgarApiScanner != null && mEgarApiScanner.isScanning(MediaType.AUDIO);
    }

    @Override
    public List getStorageDevices() throws RemoteException {
        try {
            return mEgarApiScanner.getStorageDevices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get db path.
     */
    public String getDbPath() {
        try {
            return mEgarApiScanner.getDbPath(MediaType.AUDIO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@link IPlayPresenter#setPlayPosition(int)}
     * <p>Must implement yourself</p>
     */
    @Override
    public void setPlayPosition(int position) {
    }

    /**
     * {@link IAudioPlayService#getCurrPos()}
     * <p>Must implement yourself</p>
     */
    @Override
    public int getCurrPos() {
        return 0;
    }

    /**
     * {@link IAudioPlayService#getTotalCount()}
     * <p>Must implement yourself</p>
     */
    @Override
    public int getTotalCount() {
        return 0;
    }

    /**
     * {@link IAudioPlayService#getCurrMedia()}
     * <p>Must implement yourself</p>
     */
    @Override
    public ProAudio getCurrMedia() {
        return null;
    }

    /**
     * {@link IAudioPlayService#getCurrMediaPath()}
     * <p>Must implement yourself</p>
     */
    @Override
    public String getCurrMediaPath() {
        return null;
    }

    /**
     * {@link IAudioPlayService#getProgress()}
     * <p>Must implement yourself</p>
     */
    @Override
    public long getProgress() {
        return 0;
    }

    /**
     * {@link IAudioPlayService#getDuration()}
     * <p>Must implement yourself</p>
     */
    @Override
    public long getDuration() {
        return 0;
    }

    /**
     * {@link IAudioPlayService#isPlaying()}
     * <p>Must implement yourself</p>
     */
    @Override
    public boolean isPlaying() {
        return false;
    }

    /**
     * {@link IAudioPlayService#play()}
     * <p>Must implement yourself</p>
     */
    @Override
    public void play() {
    }

    /**
     * {@link IAudioPlayService#applyPlayList(String[])}
     * <p>Must implement yourself</p>
     */
    @Override
    public void applyPlayList(String[] params) {
    }

    /**
     * {@link IAudioPlayService#applyPlayInfo(String, int)}
     * <p>Must implement yourself</p>
     */
    @Override
    public void applyPlayInfo(String mediaUrl, int pos) {
    }

    /**
     * {@link IAudioPlayService#playByUrlByUser(String)}
     * <p>Must implement yourself</p>
     */
    @Override
    public void playByUrlByUser(String mediaPath) {
    }

    /**
     * {@link IAudioPlayService#playPrevByUser()}
     * <p>Must implement yourself</p>
     */
    @Override
    public void playPrevByUser() {
    }

    /**
     * {@link IAudioPlayService#playNextByUser()}
     * <p>Must implement yourself</p>
     */
    @Override
    public void playNextByUser() {
    }

    /**
     * {@link IAudioPlayService#playOrPauseByUser()}
     * <p>Must implement yourself</p>
     */
    @Override
    public void playOrPauseByUser() {
    }

    /**
     * {@link IAudioPlayService#release()}
     * <p>Must implement yourself</p>
     */
    @Override
    public void release() {
    }

    /**
     * {@link IAudioPlayService#seekTo(int)}
     * <p>Must implement yourself</p>
     */
    @Override
    public void seekTo(int time) {
    }

    /**
     * {@link IAudioPlayService#isSeeking()}
     * <p>Must implement yourself</p>
     */
    @Override
    public boolean isSeeking() {
        return false;
    }

    /**
     * {@link IPlayPresenter#savePlayMediaInfo(String, int)}
     * <p>Must implement yourself</p>
     */
    @Override
    public void savePlayMediaInfo(String mediaPath, int progress) {
        AudioPreferUtils.getLastPlayedMediaInfo(true, mediaPath, progress);
    }

    @Override
    public String getLastMediaUrl() {
        String[] playedMediaInfo = AudioPreferUtils.getLastPlayedMediaInfo(false, "", 0);
        if (playedMediaInfo.length > 0) {
            return playedMediaInfo[0];
        }
        return "";
    }

    @Override
    public long getLastProgress() {
        String[] playedMediaInfo = AudioPreferUtils.getLastPlayedMediaInfo(false, "", 0);
        if (playedMediaInfo.length > 1) {
            try {
                return Integer.valueOf(playedMediaInfo[1]);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public void switchPlayMode(int supportFlag) {
        Logs.i(TAG, "switchPlayMode(" + PlayModeSupportType.desc(supportFlag) + ")");
        final int storePlayMode = getPlayMode();
        switch (supportFlag) {
            case PlayModeSupportType.ALL: {
                switch (storePlayMode) {
                    case PlayMode.SINGLE:
                        setPlayMode(PlayMode.RANDOM);
                        break;
                    case PlayMode.RANDOM:
                        setPlayMode(PlayMode.LOOP);
                        break;
                    case PlayMode.LOOP:
                        setPlayMode(PlayMode.ORDER);
                        break;
                    case PlayMode.ORDER:
                        setPlayMode(PlayMode.SINGLE);
                        break;
                }
            }
            break;
            case PlayModeSupportType.NO_ORDER:
            default: {
                switch (storePlayMode) {
                    case PlayMode.SINGLE:
                        setPlayMode(PlayMode.RANDOM);
                        break;
                    case PlayMode.RANDOM:
                        setPlayMode(PlayMode.LOOP);
                        break;
                    case PlayMode.LOOP:
                        setPlayMode(PlayMode.SINGLE);
                        break;
                }
            }
            break;
        }
    }

    @Override
    public void setPlayMode(int mode) {
        this.mPlayMode = mode;
        AudioPreferUtils.getPlayMode(true, this.mPlayMode);
        onPlayModeChanged(mode);
    }

    @Override
    public int getPlayMode() {
        this.mPlayMode = AudioPreferUtils.getPlayMode(false, PlayMode.LOOP);
        return this.mPlayMode;
    }

    @Override
    public void focusPlayer() {
    }

    /**
     * Execute request audio focus.
     *
     * @return {@link AudioManager#AUDIOFOCUS_REQUEST_GRANTED} or {@link AudioManager#AUDIOFOCUS_REQUEST_FAILED}
     */
    protected int reqAudioFocus() {
        if (mAudioFocusPresenter != null) {
            return mAudioFocusPresenter.reqAudioFocus();
        }
        return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
    }

    /**
     * Execute abandon audio focus.
     *
     * @return {@link AudioManager#AUDIOFOCUS_REQUEST_GRANTED} or {@link AudioManager#AUDIOFOCUS_REQUEST_FAILED}
     */
    protected int abandonAudioFocus() {
        if (mAudioFocusPresenter != null) {
            return mAudioFocusPresenter.abandonAudioFocus();
        }
        return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
    }

    @Override
    public boolean isPlayerFocused() {
        return isAudioFocusGained();
    }

    /**
     * Check audio focus status.
     *
     * @return true-GAINED
     */
    public boolean isAudioFocusGained() {
        return mAudioFocusPresenter != null && mAudioFocusPresenter.isAudioFocusGained();
    }

    /**
     * {@link IAudioPlayListener#onMountStateChanged(List)}
     */
    @Override
    public void onMountStateChanged(List listStorageDevices) {
        for (AudioPlayResp l : mListPlayListeners) {
            try {
                if (l.mmAudioPlayListener != null) {
                    l.mmAudioPlayListener.onMountStateChanged(listStorageDevices);
                }
            } catch (Exception e) {
                Logs.i(TAG, "onMountStateChanged() >> e: " + e.getMessage());
            }
        }
    }

    /**
     * {@link IAudioPlayListener#onScanStateChanged(int)}
     */
    @Override
    public void onScanStateChanged(int state) {
        Logs.i(TAG, "onScanStateChanged(" + state + ")");
        for (AudioPlayResp l : mListPlayListeners) {
            try {
                if (l.mmAudioPlayListener != null) {
                    l.mmAudioPlayListener.onScanStateChanged(state);
                }
            } catch (Exception e) {
                Logs.i(TAG, "onScanStateChanged() >> e: " + e.getMessage());
            }
        }
    }

    /**
     * {@link IAudioPlayListener#onGotDeltaMedias(List)}
     */
    @Override
    public void onGotDeltaMedias(List listMedias) {
        Logs.debugI(TAG, "onGotDeltaMedias(" + (listMedias == null ? 0 : listMedias.size()) + ")"
                + "\n    respLs -> count:" + mListPlayListeners.size());
        for (AudioPlayResp respL : mListPlayListeners) {
            try {
                Logs.debugI(TAG, "respL -> [" + respL.mmIsRespDelta + ";" + respL.mmAudioPlayListener);
                if (respL.mmAudioPlayListener != null && respL.mmIsRespDelta) {
                    respL.mmAudioPlayListener.onGotDeltaMedias(listMedias);
                }
            } catch (Exception e) {
                Logs.i(TAG, "onGotDeltaMedias() >> e: " + e.getMessage());
            }
        }
    }

    /**
     * {@link IAudioPlayListener#onPlayStateChanged(int)}
     */
    @Override
    public void onPlayStateChanged(int playStateValue) {
        for (AudioPlayResp l : mListPlayListeners) {
            try {
                if (l.mmAudioPlayListener != null) {
                    l.mmAudioPlayListener.onPlayStateChanged(playStateValue);
                }
            } catch (Exception e) {
                Logs.i(TAG, "onPlayStateChanged() >> e: " + e.getMessage());
            }
        }
    }

    /**
     * {@link IAudioPlayListener#onPlayProgressChanged(String, int, int)}
     */
    @Override
    public void onPlayProgressChanged(String mediaPath, int progress, int duration) {
        //Logs.debugI(TAG, "onPlayProgressChanged(" + mediaPath + "," + progress + "," + duration + ")");
        savePlayMediaInfo(mediaPath, progress);
        for (AudioPlayResp l : mListPlayListeners) {
            try {
                if (l.mmAudioPlayListener != null && l.mmIsRespDelta) {
                    l.mmAudioPlayListener.onPlayProgressChanged(mediaPath, progress, duration);
                }
            } catch (Exception e) {
                Logs.i(TAG, "onPlayProgressChanged() >> e: " + e.getMessage());
            }
        }
    }

    @Override
    public void onGotVoiceCmd(String action, Intent data) {
    }

    @Override
    public void onVoiceCmdClose() {
    }

    @Override
    public void onVoiceCmdPrev() {
    }

    @Override
    public void onVoiceCmdNext() {
    }

    @Override
    public void onVoiceCmdPause() {
    }

    @Override
    public void onVoiceCmdPlay() {
    }

    @Override
    public void onVoiceCmdPlay(int type, String[] params) {
    }

    /**
     * {@link IAudioPlayListener#onPlayModeChanged(int)}
     */
    @Override
    public void onPlayModeChanged(int newPlayMode) {
        for (AudioPlayResp l : mListPlayListeners) {
            try {
                if (l.mmAudioPlayListener != null) {
                    l.mmAudioPlayListener.onPlayModeChanged(newPlayMode);
                }
            } catch (Exception e) {
                Logs.i(TAG, "onPlayModeChanged() >> e: " + e.getMessage());
            }
        }
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#getAllMedias(int, String[])}
     */
    @Override
    public List getAllMedias(int sortBy, String[] params) {
        try {
            return mEgarApiScanner.getAllMedias(MediaType.AUDIO, sortBy, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#getMediasByColumns(Map, String)}
     */
    @Override
    public List getMediasByColumns(Map<String, String> mapColumns, String sortOrder) {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getMediasByColumns(MediaType.AUDIO, mapColumns, sortOrder);
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#getFilterFolders()}
     */
    @Override
    public List getFilterFolders() {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getFilterFolders(MediaType.AUDIO);
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#getFilterArtists()}
     */
    @Override
    public List getFilterArtists() {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getFilterArtists(MediaType.AUDIO);
        }
        return null;
    }

    /**
     * {@link IAudioDataOpActions#getFilterAlbums()}
     */
    @Override
    public List getFilterAlbums() {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.getFilterAlbums(MediaType.AUDIO);
        }
        return null;
    }

    /**
     * {@link IAudioPlayService#updateMediaCollect(int, ProAudio)}
     */
    @Override
    public int updateMediaCollect(int position, ProAudio media) {
        try {
            List<ProAudio> mediasToPlay = new ArrayList<>();
            mediasToPlay.add(media);
            return mEgarApiScanner.updateMediaCollect(MediaType.AUDIO, mediasToPlay);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int clearHistoryCollect() {
        if (mEgarApiScanner != null) {
            return mEgarApiScanner.clearHistoryCollect(MediaType.AUDIO);
        }
        return 0;
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#getAllMediaSheets(int)}
     */
    @Override
    public List getAllMediaSheets(int id) {
        try {
            return mEgarApiScanner.getAllMediaSheets(MediaType.AUDIO, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#addMediaSheet(List)}
     */
    @Override
    public int addMediaSheet(List mediaSheet) {
        try {
            return mEgarApiScanner.addMediaSheet(MediaType.AUDIO, mediaSheet);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#updateMediaSheet(List)}
     */
    @Override
    public int updateMediaSheet(List mediaSheet) {
        try {
            return mEgarApiScanner.updateMediaSheet(MediaType.AUDIO, mediaSheet);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#getAllMediaSheetMapInfos(int)}
     */
    @Override
    public List getAllMediaSheetMapInfos(int sheetId) {
        try {
            return mEgarApiScanner.getAllMediaSheetMapInfos(MediaType.AUDIO, sheetId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#addMediaSheetMapInfos(List)}
     */
    @Override
    public int addMediaSheetMapInfos(List listMapInfos) {
        try {
            return mEgarApiScanner.addMediaSheetMapInfos(MediaType.AUDIO, listMapInfos);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * {@link com.egar.music.api.IAudioDataOpActions#deleteMediaSheetMapInfos(int)}
     */
    @Override
    public int deleteMediaSheetMapInfos(int sheetId) {
        try {
            return mEgarApiScanner.deleteMediaSheetMapInfos(MediaType.AUDIO, sheetId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * {@link IAudioPlayService#getCountInDB()}
     */
    @Override
    public long getCountInDB() {
        try {
            return mEgarApiScanner.getCountInDB(MediaType.AUDIO);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Class to parcel {@link juns.lib.media.player.AudioPlayerFactory.IAudioPlayerListener}
     */
    private static final class AudioPlayResp {
        boolean mmIsRespDelta;
        String mmTag;
        IAudioPlayListener mmAudioPlayListener;
        DeathRecipient mmDeathRecipient;

        AudioPlayResp(boolean isRespDelta, String tag, IAudioPlayListener l) {
            mmIsRespDelta = isRespDelta;
            mmTag = tag;
            mmAudioPlayListener = l;
        }

        IBinder asBinder() {
            return mmAudioPlayListener.asBinder();
        }
    }

    /**
     * {@link IPlayPresenter#setVolume(float, float)}
     * <p>Must implement yourself</p>
     */
    @Override
    public void setVolume(float leftVolume, float rightVolume) {
    }

    /**
     * {@link juns.lib.media.player.AudioPlayerFactory.IAudioPlayerListener#onPlayerStateChanged(int)}
     */
    @Override
    public void onPlayerStateChanged(int playState) {
        onPlayStateChanged(playState);
    }

    /**
     * {@link juns.lib.media.player.AudioPlayerFactory.IAudioPlayerListener#onPlayerProgressChanged(String, int, int)}
     */
    @Override
    public void onPlayerProgressChanged(String mediaPath, int progress, int duration) {
        onPlayProgressChanged(mediaPath, progress, duration);
    }

    @Override
    public void destroy() {
        volumeFadeDestroy();
        if (mAudioFocusPresenter != null) {
            mAudioFocusPresenter.removeAudioFocusListener(this);
        }
        bindScanService(false);
    }

    /**
     * {@link MediaBtnReceiver.MediaBtnListener#onGotMediaKey(KeyEvent)}
     */
    @Override
    public void onGotMediaKey(KeyEvent event) {
    }

    /**
     * Get position of mediaUrl in playing list.
     *
     * @param list     Media playing list.
     * @param mediaUrl Media path.
     * @return Position of mediaUrl in playing list.
     */
    protected int getPosAtPlayList(List<ProAudio> list, String mediaUrl) {
        int pos = -1;
        try {
            for (int LOOP = list.size(), idx = 0; idx < LOOP; idx++) {
                ProAudio media = list.get(idx);
                if (TextUtils.equals(media.getMediaUrl(), mediaUrl)) {
                    pos = idx;
                }
            }
        } catch (Exception e) {
            pos = -1;
        }
        return pos;
    }
}