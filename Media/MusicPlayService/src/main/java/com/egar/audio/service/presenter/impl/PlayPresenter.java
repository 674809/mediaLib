package com.egar.audio.service.presenter.impl;

import android.content.Context;
import android.content.Intent;
import android.egar.MediaStatus;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.egar.audio.receiver.MediaBtnReceiver;
import com.egar.audio.service.presenter.IVoiceCmdPresenter;
import com.egar.music.api.EgarApiMusic;
import com.egar.music.api.utils.SettingsSysUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.android.utils.Utils;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.java.utils.JsFileUtils;
import juns.lib.media.action.IAudioPlayer;
import juns.lib.media.bean.MediaBase;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.db.manager.AudioDBManager;
import juns.lib.media.flags.FilterType;
import juns.lib.media.flags.KeyCodes;
import juns.lib.media.flags.PlayMode;
import juns.lib.media.flags.PlayState;
import juns.lib.media.play.IAudioPlayListener;
import juns.lib.media.player.AudioPlayerFactory;
import juns.lib.media.utils.SDCardUtils;

/**
 * Audio play presenter class
 *
 * @author Jun.Wang
 */
public class PlayPresenter extends PlayPresenterBase {
    //TAG
    private static final String TAG = "PlayPresenter";

    /**
     * {@link Context} of attached component
     */
    private Context mContext;

    /**
     * Delay play handler;
     */
    private static Handler mDelayPlayHandler = new Handler();

    /**
     * Database operate manager.
     */
    private AudioDBManager mDbManager;

    /**
     * Music Player Object
     */
    private IAudioPlayer mAudioPlayer;

    /**
     * 临时歌单
     * <p>当获取所有数据</p>
     */
    private List<ProAudio> mTempAudioList, mAudioList;
    /**
     * Current playing position
     */
    private int mCurrPos;

    /**
     * Play Next/Previous Media Flag
     */
    private boolean mIsPlayNext = false, mIsPlayPrev = false;
    /**
     * 是否在第一个媒体加载完成后执行停止播放？
     * <p>"顺序播放" 模式下，播放完成最后一个音频后，跳转到第一个媒体，停止播放</p>
     */
    private boolean mIsPauseOnFirstLoaded = false;

    /**
     * Media Button 处理
     */
    private MediaBtnPresenter mMediaBtnPresenter;

    /**
     * 异步加载媒体任务
     */
    private VoiceAssistantTask mVoiceAssistantTask;

    /**
     * 自动播放媒体任务
     * <p>(1) 首次加载列表</p>
     * <p>(2) 检测是否有上次的播放记录？如果有，则播放该记录。</p>
     * <p>(3) 否则选择播放列表中的第一首进行播放</p>
     */
    private AutoPlayTask mAutoPlayTask;

    /**
     * 解析并播放 任务
     * <p>根据媒体路径解析并播放</p>
     */
    private ParseAndPlayTask mParseAndPlayTask;

    /**
     * 异步加载媒体任务
     */
    private AsyncPlayByUrlTask mAsyncPlayByUrlTask;

    /**
     * 语音命令处理
     */
    private IVoiceCmdPresenter mVoiceCmdPresenter;

    public PlayPresenter(Context context) {
        super(context);
        mContext = context;
        mDbManager = AudioDBManager.instance(context);
        //reqAudioFocus();
    }

    @Override
    public void volumeResetAndFadeIn() {
        super.volumeResetAndFadeIn();
    }

    @Override
    public void volumeResetAndFadeOut() {
        super.volumeResetAndFadeOut();
    }

    @Override
    public void volumeFadeDestroy() {
        super.volumeFadeDestroy();
    }

    @Override
    public void onVolumeFadeChanged(float leftVolume, float rightVolume) {
        //        super.onVolumeFadeChanged(leftVolume, rightVolume);
        setVolume(leftVolume, rightVolume);
    }

    @Override
    public void onAudioFocusDuck() {
        //super.onAudioFocusDuck();
        Logs.i(TAG, "onAudioFocusDuck()");
    }

    @Override
    public void onAudioFocusTransient() {
        //super.onAudioFocusTransient();
        Logs.i(TAG, "onAudioFocusTransient()");
        pause();
    }

    @Override
    public void onAudioFocusGain() {
        //super.onAudioFocusGain();
        Logs.i(TAG, "onAudioFocusGain()");
        registerMediaBtn(true);
        if (getVoiceCmdPresenter().hasVoiceCmd()) {
            getVoiceCmdPresenter().processVoiceCmd();
        } else {
            resume();
        }
    }

    @Override
    public void onAudioFocusLoss() {
        //super.onAudioFocusLoss();
        Logs.i(TAG, "onAudioFocusLoss()");
        resetPlayer(false);
    }

    @Override
    public void onMediaScanServiceConnected() {
        Logs.i(TAG, "onMediaScanServiceConnected()");
        super.onMediaScanServiceConnected();
        mDbManager.setDbPath(getDbPath());

        // TEST CODE, DON`T REMOVE
        //String[] params = new String[6];
        //params[1] = "太幸福";
        //getAllMedias(FilterType.MEDIA_NAME, params);
    }

    /**
     * Used by client of AudioPlayService.
     * <p>You had better don`t use this method in service.</p>
     */
    @Override
    public void autoPlay() {
        Logs.i(TAG, "autoPlay() -START-");
        if (!EgarApiMusic.isPlayEnable(mContext)) {
            Logs.i(TAG, "autoPlay() is blocked because of isPlayEnable() is false.");
            return;
        }
        if (!isAudioFocusGained()) {
            Logs.i(TAG, "autoPlay() is blocked because of isAudioFocusGained() is false.");
            return;
        }
        if (isPlaying()) {
            Logs.i(TAG, "autoPlay() is blocked because of isPlaying() is true.");
            return;
        }

        //执行刷新
        Logs.i(TAG, "autoPlay() -EXEC TASK-");
        if (mAutoPlayTask != null) {
            mAutoPlayTask.cancel(true);
            mAutoPlayTask = null;
        }
        //EXEC task
        mAutoPlayTask = new AutoPlayTask(this);
        mAutoPlayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 自动播放任务
     * <p>(1) 首次加载列表</p>
     * <p>(2) 检测是否有上次的播放记录？如果有，则播放该记录。</p>
     * <p>(3) 否则选择播放列表中的第一首进行播放</p>
     */
    private static class AutoPlayTask extends AsyncTask<Void, Void, List<ProAudio>> {
        //Used to call back.
        private WeakReference<PlayPresenter> mmWeakReferencePresenter;

        /**
         * @param presenter {@link PlayPresenter}
         */
        AutoPlayTask(PlayPresenter presenter) {
            mmWeakReferencePresenter = new WeakReference<>(presenter);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<ProAudio> doInBackground(Void... voids) {
            try {
                Logs.i(TAG, "AutoPlayTask - doInBackground()");
                PlayPresenter playPresenter = mmWeakReferencePresenter.get();
                if (playPresenter.mAudioList == null || playPresenter.mAudioList.size() == 0) {
                    return playPresenter.getAllMedias(FilterType.MEDIA_NAME, null);
                } else {
                    return playPresenter.mAudioList;
                }
            } catch (Exception e) {
                Log.i(TAG, "AutoPlayTask - doInBackground() >> e: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ProAudio> listMedias) {
            super.onPostExecute(listMedias);
            try {
                Logs.i(TAG, "AutoPlayTask - onPostExecute()");
                PlayPresenter playPresenter = mmWeakReferencePresenter.get();
                playPresenter.mTempAudioList = null;
                playPresenter.mAudioList = listMedias;

                //
                String lastMediaUrl = playPresenter.getLastMediaUrl();
                Logs.i(TAG, "AutoPlayTask - onPostExecute() : mediaUrl - " + lastMediaUrl);
                playPresenter.playByUrlByUser(lastMediaUrl);
            } catch (Exception e) {
                Log.i(TAG, "AutoPlayTask - onPostExecute() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMediaScanServiceDisconnected() {
        Logs.i(TAG, "onMediaScanServiceDisconnected()");
        super.onMediaScanServiceDisconnected();
    }

    @Override
    public void onRespMountChange(List listStorageDevices) {
        Logs.i(TAG, "onRespMountChange()");
        super.onRespMountChange(listStorageDevices);
    }

    @Override
    public void onRespScanState(int state) throws RemoteException {
        //Logs.i(TAG, "onRespScanState(" + MediaScanState.desc(state) + ")");
        super.onRespScanState(state);
    }

    @Override
    public void onRespDeltaMedias(List listMedias) throws RemoteException {
        Logs.i(TAG, "onRespDeltaMedias(listMedias)");
        super.onRespDeltaMedias(listMedias);
    }

    @Override
    public void addPlayListener(boolean isRespDelta, String tag, IAudioPlayListener l) {
        Logs.i(TAG, "addPlayListener(" + tag + ")");
        super.addPlayListener(isRespDelta, tag, l);
    }

    @Override
    public void removePlayListener(String tag, IAudioPlayListener l) {
        Logs.i(TAG, "removePlayListener(" + tag + ")");
        super.removePlayListener(tag, l);
    }

    @Override
    public boolean isScanServiceConnected() {
        return super.isScanServiceConnected();
    }

    @Override
    public void startScan() {
        Logs.i(TAG, "startScan()");
        super.startScan();
    }

    @Override
    public boolean isScanning() throws RemoteException {
        return super.isScanning();
    }

    @Override
    public List getStorageDevices() throws RemoteException {
        return super.getStorageDevices();
    }

    @Override
    public void setPlayPosition(int position) {
        //        super.setPlayPosition(position);
        Logs.i(TAG, "setPlayPosition(" + position + ")");
        mCurrPos = position;
    }

    @Override
    public int getCurrPos() {
        //        return super.getCurrPos();
        return mCurrPos;
    }

    @Override
    public int getTotalCount() {
        if (mAudioList != null) {
            return mAudioList.size();
        }
        return 0;
    }

    @Override
    public ProAudio getCurrMedia() {
        try {
            return mAudioList.get(getCurrPos());
        } catch (Exception e) {
            Log.i(TAG, "ERROR :: getCurrMedia() > " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getCurrMediaPath() {
        return (mAudioPlayer == null) ? "" : mAudioPlayer.getMediaPath();
    }

    @Override
    public long getProgress() {
        return (mAudioPlayer == null) ? 0 : mAudioPlayer.getMediaTime();
    }

    @Override
    public long getDuration() {
        return (mAudioPlayer == null) ? 0 : mAudioPlayer.getMediaDuration();
    }

    @Override
    public boolean isPlaying() {
        return mAudioPlayer != null && mAudioPlayer.isMediaPlaying();
    }

    @Override
    public void play() {
        Logs.i(TAG, "play()");
        ProAudio media = getCurrMedia();
        if (media != null) {
            playFixedMedia(media.getMediaUrl());
        }
    }

    /**
     * 播放指定位置媒体
     * <p>
     * 调用此方法的时候一定要确定已经设置了播放位置
     */
    private void playFixedMedia(String mediaUrl) {
        Logs.i(TAG, "playFixedMedia(" + mediaUrl + ")");
        if (!JsFileUtils.isFileExist(mediaUrl)) {
            Log.i(TAG, "playFixedMedia() -> Forbidden for file is not exist.");
            onPlayStateChanged(PlayState.ERROR_FILE_NOT_EXIST);
            return;
        }

        // Check if player need switch?
        boolean isPlayerNeedSwitch = false;
        if (mAudioPlayer == null) {
            isPlayerNeedSwitch = true;
        }

        //初始化
        if (isPlayerNeedSwitch) {
            mAudioPlayer = AudioPlayerFactory.instance().create(mContext, mediaUrl, this);
            onPlayStateChanged(PlayState.REFRESH_UI);
        }

        // Start play fixed media.
        startPlay(mediaUrl);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyPlayList(String[] params) {
        //super.applyPlayList(filterType, params);
        Logs.i(TAG, "applyPlayList() -- Apply temp list!!! --");
        mTempAudioList = getAllMedias(FilterType.MEDIA_NAME, params);
    }

    @Override
    public void applyPlayInfo(String mediaUrl, int pos) {
        //        super.applyPlayInfo(mediaUrl, pos);
        Logs.i(TAG, "applyPlayInfo(" + mediaUrl + "," + pos + ")");
        try {
            // Merge temp list to playing list.
            if (mTempAudioList != null) {
                mAudioList = mTempAudioList;
                mTempAudioList = null;
            }

            // -- 矫正播放位置 --
            // 获取 [选中的位置] 在播放列表中对应的媒体路径
            String mediaUrlOfThisPos = mAudioList.get(pos).getMediaUrl();
            // 如果 [[选中的位置] 在播放列表中对应的媒体路径] 等同于 [选中的媒体路径]，说明播放位置是精确的。
            if (TextUtils.equals(mediaUrl, mediaUrlOfThisPos)) {
                mCurrPos = pos;
                // 位置不精确，需要根据 [选中的媒体路径] 来重新计算播放位置
            } else {
                Logs.i(TAG, "applyPlayInfo() -LOOP filter selected.-");
                mCurrPos = getPosAtPlayList(mAudioList, mediaUrl);
            }
            if (mCurrPos < 0) {
                mCurrPos = 0;
            }
        } catch (Exception e) {
            mCurrPos = 0;
        }
    }

    @Override
    public void playByUrlByUser(String mediaPath) {
        try {
            Logs.i(TAG, "playByUrlByUser(" + mediaPath + ")");
            EgarApiMusic.pausedByUser(mContext, false);
            reqAudioFocus();
            playByUrl(mediaPath);
        } catch (Exception e) {
            Log.i(TAG, "playByUrlByUser(mediaPath) >> [e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playByUrl(String mediaPath) {
        Logs.i(TAG, "playByUrl(" + mediaPath + ")");
        //Reset history play information.
        String lastMediaUrl = getLastMediaUrl();
        Logs.i(TAG, "playByUrl(mediaPath) : mediaUrl - " + lastMediaUrl);
        if (!TextUtils.equals(mediaPath, lastMediaUrl)) {
            Logs.i(TAG, "PLAY_NEW - RESET_HISTORY !!!");
            savePlayMediaInfo("", 0);
        }
        //Executed task to play by url.
        if (mAsyncPlayByUrlTask != null) {
            mAsyncPlayByUrlTask.cancel(true);
            mAsyncPlayByUrlTask = null;
        }
        //EXEC
        mAsyncPlayByUrlTask = new AsyncPlayByUrlTask(this, mediaPath, mAudioList);
        mAsyncPlayByUrlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class AsyncPlayByUrlTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<PlayPresenter> mmWeakReferencePresenter;
        private String mmMediaUrlToPlay;
        private List<ProAudio> mmListMedias;

        AsyncPlayByUrlTask(PlayPresenter presenter, String mediaUrl, List<ProAudio> listMedias) {
            mmWeakReferencePresenter = new WeakReference<>(presenter);
            mmMediaUrlToPlay = mediaUrl;
            if (listMedias != null) {
                mmListMedias = new ArrayList<>(listMedias);
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                Logs.i(TAG, "AsyncPlayByUrlTask >> doInBackground()");
                if (mmListMedias != null) {
                    for (int LOOP = mmListMedias.size(), idx = 0; idx < LOOP; idx++) {
                        ProAudio tmpMedia = mmListMedias.get(idx);
                        if (TextUtils.equals(mmMediaUrlToPlay, tmpMedia.getMediaUrl())) {
                            return idx;
                        }
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "AsyncPlayByUrlTask - doInBackground() >> e: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer position) {
            super.onPostExecute(position);
            Logs.i(TAG, "AsyncPlayByUrlTask >> onPostExecute()");
            if (position == null) {
                position = 0;
            }
            if (mmListMedias != null) {
                PlayPresenter playPresenter = mmWeakReferencePresenter.get();
                if (playPresenter != null) {
                    playPresenter.mCurrPos = position;
                    mmWeakReferencePresenter.get().playByPos(position);
                }
            }
        }
    }

    private void playByPos(int pos) {
        //        super.playByPos(pos);
        Logs.i(TAG, "playByPos(" + pos + ")");
        setPlayPosition(pos);
        play();
    }

    /**
     * If you want play Music, this is the finally method that must be execute.
     */
    private void startPlay(String mediaUrl) {
        Logs.i(TAG, "startPlay(" + mediaUrl + ")");
        if (EgarApiMusic.isPlayEnable(mContext)) {
            if (EmptyUtil.isEmpty(mediaUrl)) {
                mAudioPlayer.playMedia();
            } else {
                mAudioPlayer.playMedia(mediaUrl);
            }
        }
    }

    /**
     * 高频点击时，播放延迟时间，
     * <p>主要是为了防止短时间内，连续点击播放 [下一个/上一个] 等命令</p>
     */
    private long getPlayDelayTimeOfHighFreqClick() {
        long delayTime = getVolumeFadePeriod();
        if (delayTime == 0) {
            return 500; // 默认500ms
        }
        // {播放延迟时间} 需要比 {声音 [淡入/淡出] 时间} 需要大一些
        // 以保证：声音 [淡入/淡出]后，才能执行播放事件.
        return delayTime + 100;
    }

    private void playPrev() {
        Logs.i(TAG, "^^ playPrev() ^^");
        try {
            //
            mIsPlayPrev = true;
            setPlayPosByMode(2);
            volumeResetAndFadeOut();
            Log.i(TAG, "mCurrPos - " + mCurrPos);

            //Exec play runnable
            //防止高频点击，即用户在短时间内频繁点击执行下一个操作
            clearAllRunnable();
            mDelayPlayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    savePlayMediaInfo("", 0);
                    play();
                }
            }, getPlayDelayTimeOfHighFreqClick());
        } catch (Throwable e) {
            Log.i(TAG, "playPrev() >> e: " + e.getMessage());
        }
    }

    @Override
    public void playPrevByUser() {
        Logs.i(TAG, "^^ playPrevByUser() ^^");
        //        super.playPrevByUser();
        EgarApiMusic.pausedByUser(mContext, false);
        reqAudioFocus();
        playPrev();
    }

    private void playNext() {
        Logs.i(TAG, "^^ playNext() ^^");
        try {
            //
            mIsPlayNext = true;
            setPlayPosByMode(1);
            volumeResetAndFadeOut();
            Log.i(TAG, "mCurrPos - " + mCurrPos);

            //Exec play runnable
            //防止高频点击，即用户在短时间内频繁点击执行下一个操作
            clearAllRunnable();
            mDelayPlayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    savePlayMediaInfo("", 0);
                    play();
                }
            }, getPlayDelayTimeOfHighFreqClick());
        } catch (Throwable e) {
            Log.i(TAG, "playNext() >> e: " + e.getMessage());
        }
    }

    @Override
    public void playNextByUser() {
        Logs.i(TAG, "^^ playNextByUser() ^^");
        EgarApiMusic.pausedByUser(mContext, false);
        reqAudioFocus();
        playNext();
    }

    /**
     * Random select to play.
     */
    public void playRandom() {
        Logs.i(TAG, "^^ playRandom() ^^");
        setRandomPos();
        play();
    }

    private void playRandomByUser() {
        Logs.i(TAG, "^^ playRandomByUser() ^^");
        EgarApiMusic.pausedByUser(mContext, false);
        reqAudioFocus();
        playRandom();
    }

    /**
     * Set Play position by Play Mode
     *
     * @param flag : 1 means play next
     *             <p>
     *             2 means play previous
     */
    private void setPlayPosByMode(int flag) {
        int storePlayMode = getPlayMode();
        // MODE : RANDOM
        switch (storePlayMode) {
            case PlayMode.RANDOM:
                setRandomPos();
                break;

            // SINGLE/LOOP/ORDER
            case PlayMode.SINGLE:
            case PlayMode.LOOP:
            case PlayMode.ORDER:
            default:
                switch (flag) {
                    case 1:
                        setNextPos();
                        break;
                    case 2:
                        setPrevPos();
                        break;
                }
                break;
        }
    }

    /**
     * Set next position by random method.
     */
    private void setRandomPos() {
        if (!EmptyUtil.isEmpty(mAudioList)) {
            mCurrPos = Utils.getRandomNum(mCurrPos, getTotalCount());
        }
    }

    /**
     * Set next position of queue.
     */
    private void setNextPos() {
        mCurrPos++;
        if (mCurrPos >= getTotalCount()) {
            mCurrPos = 0;
        }
    }

    /**
     * Set previous position of queue.
     */
    private void setPrevPos() {
        mCurrPos--;
        if (mCurrPos < 0) {
            mCurrPos = getTotalCount() - 1;
        }
    }

    @Override
    public void playOrPauseByUser() {
        //        super.playOrPauseByUser();
        Logs.i(TAG, "^^ playOrPauseByUser() ^^");
        if (isPlaying()) {
            pauseByUser();
        } else {
            resumeByUser();
        }
    }

    /**
     * EXEC pause by user
     */
    private void pauseByUser() {
        Logs.i(TAG, "^^ pauseByUser() ^^");
        EgarApiMusic.pausedByUser(mContext, true);
        pause();
    }

    private void pause() {
        Logs.i(TAG, "^^ pause() ^^");
        clearAllRunnable();
        if (mAudioPlayer != null) {
            mAudioPlayer.pauseMedia();
        }
    }

    /**
     * EXEC resume by user
     */
    private void resumeByUser() {
        Logs.i(TAG, "^^ resumeByUser() ^^");
        EgarApiMusic.pausedByUser(mContext, false);
        reqAudioFocus();
        resume();
    }

    private void resume() {
        Logs.i(TAG, "^^ resume() ^^");
        try {
            clearAllRunnable();
            if (!EmptyUtil.isEmpty(mAudioList)) {
                if (mAudioPlayer == null) {
                    String lastMediaUrl = getLastMediaUrl();
                    Logs.i(TAG, "resume() : mediaUrl - " + lastMediaUrl);
                    playFixedMedia(lastMediaUrl);
                } else {
                    startPlay("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        Logs.i(TAG, "release() --Reset Player Information--");
        mAudioPlayer = null;
        clearAllRunnable();
        AudioPlayerFactory.instance().destroy();
    }

    // Remove all runnable
    private void clearAllRunnable() {
        Logs.i(TAG, "clearAllRunnable()");
        mDelayPlayHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void seekTo(int time) {
        Logs.i(TAG, "seekTo(" + time + ")");
        if (mAudioPlayer != null) {
            mAudioPlayer.seekMediaTo(time);
        }
    }

    @Override
    public boolean isSeeking() {
        //        return super.isSeeking();
        return mAudioPlayer != null && mAudioPlayer.isMediaSeeking();
    }

    @Override
    public void savePlayMediaInfo(String mediaPath, int progress) {
        Logs.i(TAG, "savePlayMediaInfo() >> mAudioPlayer:" + mAudioPlayer);
        if (mAudioPlayer != null) { //播放器状态正常
            Logs.debugI(TAG, "savePlayMediaInfo(" + mediaPath + "," + progress + ")");
            super.savePlayMediaInfo(mediaPath, progress);
        }
    }

    @Override
    public String getLastMediaUrl() {
        return super.getLastMediaUrl();
    }

    @Override
    public long getLastProgress() {
        return super.getLastProgress();
    }

    @Override
    public void switchPlayMode(int supportFlag) {
        super.switchPlayMode(supportFlag);
    }

    @Override
    public void setPlayMode(int mode) {
        super.setPlayMode(mode);
    }

    @Override
    public int getPlayMode() {
        return super.getPlayMode();
    }

    @Override
    public void focusPlayer() {
        Logs.i(TAG, "focusPlayer()");
        // super.focusPlayer();
        // Register MEDIA_BUTTON when audio focus is GAINED.
        if (isAudioFocusGained()) {
            registerMediaBtn(true);
            // Else request audio focus.
        } else {
            reqAudioFocus();
        }

        // Paused by user.
        if (EgarApiMusic.isPausedByUser(mContext)) {
            Logs.i(TAG, "focusPlayer() is blocked because of isPausedByUser(Context) is true.");
            updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
            return;
        }
        // Audio focus didn't gain.
        if (!isAudioFocusGained()) {
            Logs.i(TAG, "focusPlayer() is blocked because of isAudioFocusGained() is false.");
            updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
            return;
        }

        // Try to resume
        if (EmptyUtil.isEmpty(mAudioList)) {
            //执行刷新
            long mediaCountInDB = getCountInDB();
            Logs.i(TAG, "focusPlayer() >>[mediaCountInDB: " + mediaCountInDB);
            if (mediaCountInDB <= 0) {
                startScan();
            }
        } else if (!isPlaying()) {
            Logs.i(TAG, "{mTempAudioList: " + mTempAudioList + ", mAudioList.size():" + mAudioList.size() + "}");
            resume();
        }

        //Update status bar according playing status.
        updateStatusBar(-1);
    }

    @Override
    protected int reqAudioFocus() {
        Logs.i(TAG, "reqAudioFocus()");
        int result = super.reqAudioFocus();
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            registerMediaBtn(true);
            getVoiceCmdPresenter().processVoiceCmd();
        }
        return super.reqAudioFocus();
    }

    @Override
    protected int abandonAudioFocus() {
        Logs.i(TAG, "abandonAudioFocus()");
        return super.abandonAudioFocus();
    }

    @Override
    public boolean isAudioFocusGained() {
        return super.isAudioFocusGained();
    }

    @Override
    public void onMountStateChanged(List listStorageDevices) {
        Logs.i(TAG, "onMountStateChanged()");
        super.onMountStateChanged(listStorageDevices);
        Map<String, StorageDevice> mapStorage = SDCardUtils.getMapMountedUsb(mContext);
        if (mapStorage == null || mapStorage.size() == 0) {
            Logs.i(TAG, "onMountStateChanged() -resetPlayer-");
            resetPlayer(true);
        }
    }

    @Override
    public void onScanStateChanged(int state) {
        super.onScanStateChanged(state);
    }

    @Override
    public void onGotDeltaMedias(List listMedias) {
        super.onGotDeltaMedias(listMedias);
    }

    @Override
    public void onPlayStateChanged(final int playState) {
        Logs.i(TAG, "onPlayStateChanged(" + PlayState.desc(playState) + ")");
        super.onPlayStateChanged(playState);
        switch (playState) {
            case PlayState.PLAY:
                if (isAudioFocusGained() && EgarApiMusic.isPlayEnable(mContext)) {
                    updateStatusBar(MediaStatus.MEDIA_STATUS_PLAYING);
                } else {
                    updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
                }
                break;
            case PlayState.PREPARED:
                if (isAudioFocusGained() && EgarApiMusic.isPlayEnable(mContext)) {
                    volumeResetAndFadeIn();
                    onMediaPrepared();
                    updateStatusBar(MediaStatus.MEDIA_STATUS_PLAYING);
                } else {
                    pause();
                    updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
                }
                break;
            case PlayState.REFRESH_UI:
                break;
            case PlayState.SEEK_COMPLETED:
                break;
            case PlayState.COMPLETE:
                updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
                savePlayMediaInfo("", 0);
                if (isAudioFocusGained()) {
                    playAuto();
                }
                break;
            case PlayState.ERROR:
            case PlayState.ERROR_FILE_NOT_EXIST:
                release();
                updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
                if (isAudioFocusGained()) {
                    onMediaError(true);
                }
                break;
            case PlayState.ERROR_PLAYER_INIT:
                release();
                updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
                if (isAudioFocusGained()) {
                    onMediaError(false);
                }
                break;
            default:
                updateStatusBar(MediaStatus.MEDIA_STATUS_PAUSE);
                break;
        }
    }

    private void onMediaPrepared() {
        Logs.i(TAG, "^^ onMediaPrepared() ^^");
        try {
            mIsPlayNext = false;
            mIsPlayPrev = false;
            if (mIsPauseOnFirstLoaded) {
                mIsPauseOnFirstLoaded = false;
                release();
            } else {
                long lastProgress = getLastProgress();
                if (lastProgress > 0) {
                    seekTo((int) lastProgress);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AUTO Select to play
     */
    private void playAuto() {
        Logs.i(TAG, "^^ playAuto() ^^");
        int storePlayMode = getPlayMode();
        // 如果是播放模式是“顺序模式”，并且已经播放完毕了最后一个，那么下面的动作是在跳转到第一个媒体后，停止播放
        if (storePlayMode == PlayMode.ORDER) {
            if (mCurrPos >= (getTotalCount() - 1)) {
                mIsPauseOnFirstLoaded = true;
            }
        }

        // 如果不是单曲循环，获取下一个播放位置
        if (storePlayMode != PlayMode.SINGLE) {
            setPlayPosByMode(1);
        }
        play();
    }

    private void onMediaError(boolean isSeriousError) {
        Log.i(TAG, "^^ onMediaError(" + isSeriousError + ") ^^");
        try {
            //之前执行的动作是播放上一个
            if (mIsPlayPrev) {
                Log.i(TAG, "onMediaError - mIsPlayPrev");
                mIsPlayPrev = false;
                playPrev();
                //之前执行的动作是播放下一个
            } else if (mIsPlayNext) {
                Log.i(TAG, "onMediaError - mIsPlayNext");
                mIsPlayNext = false;
                playNext();
            } else {
                int playMode = getPlayMode();
                Log.i(TAG, "onMediaError - playMode : " + playMode);
                if (playMode == PlayMode.SINGLE) {
                    if (isSeriousError) {
                        Log.i(TAG, "onMediaError - SINGLE : playNext()");
                        playNext();
                    } else {
                        Log.i(TAG, "onMediaError - SINGLE : replay");
                        playByUrl(getCurrMediaPath());
                    }
                } else {
                    Log.i(TAG, "onMediaError - playNext()");
                    playNext();
                }
            }
            super.onPlayStateChanged(PlayState.ERROR);
        } catch (Exception e) {
            Logs.i(TAG, "onMediaError(isSeriousError) >> e: " + e.getMessage());
        }
    }

    @Override
    public void onPlayProgressChanged(String mediaPath, int progress, int duration) {
        super.onPlayProgressChanged(mediaPath, progress, duration);
    }

    @Override
    public void onGotVoiceCmd(String action, Intent data) {
        super.onGotVoiceCmd(action, data);
        Logs.i(TAG, "onGotVoiceCmd(" + action + ", data)");
        getVoiceCmdPresenter().parseVoiceCmd(action, data);
        if (isAudioFocusGained()) {
            getVoiceCmdPresenter().processVoiceCmd();
        }
    }

    @Override
    public void onVoiceCmdClose() {
        //super.onVoiceCmdClose();
        Logs.i(TAG, "onVoiceCmdClose()");
        resetPlayer(true);
    }

    @Override
    public void onVoiceCmdPrev() {
        //        super.onVoiceCmdPrev();
        Logs.i(TAG, "onVoiceCmdPrev()");
        playPrevByUser();
    }

    @Override
    public void onVoiceCmdNext() {
        //        super.onVoiceCmdNext();
        Logs.i(TAG, "onVoiceCmdNext()");
        playNextByUser();
    }

    @Override
    public void onVoiceCmdPause() {
        //        super.onVoiceCmdPause();
        Logs.i(TAG, "onVoiceCmdPause()");
        pauseByUser();
    }

    @Override
    public void onVoiceCmdPlay() {
        //        super.onVoiceCmdPlay();
        Logs.i(TAG, "onVoiceCmdPlay()");
        resumeByUser();
    }

    @Override
    public void onVoiceCmdPlay(int type, String[] params) {
        //        super.onVoiceCmdPlay(type, params);
        String title = "";
        String artist = "";
        String path = "";
        if (params != null) {
            if (params.length >= 1) {
                title = params[0];
            }
            if (params.length >= 2) {
                artist = params[1];
            }
            if (params.length >= 3) {
                path = params[2];
            }
        }
        Log.i(TAG, " -onVoiceCmdPlay() - "
                + "\n        [type:" + type + "]"
                + "\n        [title:" + title + "]"
                + "\n        [artist:" + artist + "]"
                + "\n        [path:" + path + "]");

        switch (type) {
            //1==》打开并播放歌曲==>"我要听歌"
            //    执行正常的打开并自动播放流程即可了。
            case 1:
                playAudioType_1_2(false);
                break;
            //2==》随机播放一首歌曲
            case 2:
                playAudioType_1_2(true);
                break;
            //3==》播放指定歌手的歌曲。歌手的名称：请看artist参数值; 歌曲路径看path参数值（如果path没值，播放器自己定义播放这个歌手的哪首歌曲）
            // 根据歌手随便选择一首歌曲，如果已经正在播放刘德华的歌曲，不切换。
            case 3:
                playAudioType_3_5(title, artist);
                break;
            //4==》播放指定歌名的歌曲。“我要听华阴老腔一声喊”     歌曲路径看path参数值（必须要有path）;
            // 必须根据PATH来判断播放哪首歌曲
            case 4:
                playAudioType4(path);
                break;
            //5==》播放指定歌手指定歌名的歌曲。歌手的名称:请看artist参数值（必须要有artist）; 歌曲路径看path参数值（必须要有path）
            case 5:
                playAudioType_3_5(title, artist);
                break;
            default:
                break;
        }
    }

    /**
     * <p>== 1 ==</p>
     * <p>    打开并播放歌曲</p>
     * <p>== 2 ==</p>
     * <p>    随机挑选一首歌进行播放。</p>
     *
     * @param isRandomOneInAll true 在所有媒体的列表中选择一个歌曲进行播放。
     */
    private void playAudioType_1_2(boolean isRandomOneInAll) {
        Log.i(TAG, "<^-^> Play selected !!! <^-^>");
        if (mVoiceAssistantTask != null) {
            mVoiceAssistantTask.cancel(true);
            mVoiceAssistantTask = null;
        }
        //EXEC task
        mVoiceAssistantTask = new VoiceAssistantTask(this, isRandomOneInAll);
        mVoiceAssistantTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * <p>== 3 ==</p>
     * <p>    根据 [歌手] 随便选择一首歌曲，如果刘德华的歌；已经正在播放刘德华的歌曲，不切换。</p>
     * <p>== 5 ==</p>
     * <p>    根据 [歌手 & 歌名] 随便选择一首歌曲，如"刘德华的冰雨",如果正在播放的歌曲已经是"刘德华的冰雨"，不切换。</p>
     */
    private void playAudioType_3_5(String mediaName, String artist) {
        Log.i(TAG, "playAudioType_3_5(" + mediaName + "," + artist + ")");
        if (mVoiceAssistantTask != null) {
            mVoiceAssistantTask.cancel(true);
            mVoiceAssistantTask = null;
        }
        //EXEC task
        mVoiceAssistantTask = new VoiceAssistantTask(this, mediaName, artist);
        mVoiceAssistantTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * //4==》播放指定歌名的歌曲。“我要听华阴老腔一声喊”     歌曲路径看path参数值（必须要有path）;
     * <p>必须根据PATH来判断播放哪首歌曲</p>
     */
    private void playAudioType4(String path) {
        Log.i(TAG, "playAudioType4(" + path + ")");
        // Check empty.
        if (EmptyUtil.isEmpty(path)) {
            Log.i(TAG, "<..> Bad news , your file is empty!!! -1- <..>");
            return;
        }

        //
        MediaBase mediaBase = mDbManager.getMedia(path);
        //媒体不在列表中
        if (mediaBase == null) {
            Log.i(TAG, "<^-^> Play after parsing. -1- !!! <^-^>");
            parseAndPlayNewMedia(path);
            return;
        }

        //媒体在列表中
        Log.i(TAG, "<^-^> Play selected !!! <^-^>");
        if (mVoiceAssistantTask != null) {
            mVoiceAssistantTask.cancel(true);
            mVoiceAssistantTask = null;
        }
        //EXEC task
        mVoiceAssistantTask = new VoiceAssistantTask(this, path);
        mVoiceAssistantTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPlayModeChanged(int newPlayMode) {
        super.onPlayModeChanged(newPlayMode);
    }

    @Override
    public List getMediasByColumns(Map<String, String> mapColumns, String sortOrder) {
        return super.getMediasByColumns(mapColumns, sortOrder);
    }

    @Override
    public List getAllMedias(int sortBy, String[] params) {
        return super.getAllMedias(sortBy, params);
    }

    @Override
    public List getFilterFolders() {
        return super.getFilterFolders();
    }

    @Override
    public List getFilterArtists() {
        return super.getFilterArtists();
    }

    @Override
    public List getFilterAlbums() {
        return super.getFilterAlbums();
    }

    @Override
    public int updateMediaCollect(int position, ProAudio media) {
        Logs.i(TAG, "updateMediaCollect(" + position + "," + media + ")");
        int res = super.updateMediaCollect(position, media);
        if (res > 0) {
            try {
                ProAudio mediaOfPos = mAudioList.get(position);
                if (TextUtils.equals(mediaOfPos.getMediaUrl(), media.getMediaUrl())) {
                    mediaOfPos.setCollected(media.getCollected());
                    //Loop and filter same media from playing list.
                } else if (!EmptyUtil.isEmpty(mAudioList)) {
                    for (ProAudio tmpAudio : mAudioList) {
                        if (TextUtils.equals(tmpAudio.getMediaUrl(), media.getMediaUrl())) {
                            mediaOfPos = tmpAudio;
                            mediaOfPos.setCollected(media.getCollected());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Logs.i(TAG, "updateMediaCollect() >> e: " + e.getMessage());
            }
        }
        return res;
    }

    @Override
    public int clearHistoryCollect() {
        Logs.i(TAG, "clearHistoryCollect()");
        return super.clearHistoryCollect();
    }

    @Override
    public List getAllMediaSheets(int id) {
        return super.getAllMediaSheets(id);
    }

    @Override
    public int addMediaSheet(List mediaSheet) {
        Logs.i(TAG, "addMediaSheet(" + mediaSheet + ")");
        return super.addMediaSheet(mediaSheet);
    }

    @Override
    public int updateMediaSheet(List mediaSheet) {
        Logs.i(TAG, "updateMediaSheet(" + mediaSheet + ")");
        return super.updateMediaSheet(mediaSheet);
    }

    @Override
    public List getAllMediaSheetMapInfos(int sheetId) {
        return super.getAllMediaSheetMapInfos(sheetId);
    }

    @Override
    public int addMediaSheetMapInfos(List listMapInfos) {
        Logs.i(TAG, "addMediaSheetMapInfos(" + listMapInfos + ")");
        return super.addMediaSheetMapInfos(listMapInfos);
    }

    @Override
    public int deleteMediaSheetMapInfos(int sheetId) {
        Logs.i(TAG, "deleteMediaSheetMapInfos(" + sheetId + ")");
        return super.deleteMediaSheetMapInfos(sheetId);
    }

    @Override
    public long getCountInDB() {
        return super.getCountInDB();
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
//        super.setVolume(leftVolume, rightVolume);
        if (mAudioPlayer != null) {
            mAudioPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    @Override
    public void onPlayerStateChanged(int playState) {
        super.onPlayerStateChanged(playState);
    }

    @Override
    public void onPlayerProgressChanged(String mediaPath, int progress, int duration) {
        super.onPlayerProgressChanged(mediaPath, progress, duration);
    }

    @Override
    public void destroy() {
        Logs.i(TAG, "destroy()");
        resetPlayer(true);
        super.destroy();
    }

    private void resetPlayer(boolean isRelease) {
        Logs.i(TAG, "resetPlayer(isRelease: " + isRelease + ")");
        //Clear all runnable that maybe running now.
        clearAllRunnable();
        // Destroy voice command.
        resetVoiceCmdPresenter();
        //Abandon audio focus.
        abandonAudioFocus();
        //Remove MEDIA_BUTTON.
        registerMediaBtn(false);

        //Release player
        if (isRelease) {
            release();
            //Reset play information.
            mTempAudioList = null;
            mAudioList = null;
            mCurrPos = 0;
            //Reset paused flag.
            EgarApiMusic.pausedByUser(mContext, false);

            //Only pause.
        } else {
            pause();
        }

        //Set play status of bottom bar.
        updateStatusBar(MediaStatus.MEDIA_STATUS_STOP);
    }

    /**
     * 注册媒体按键事件
     * <P>为了防止注册失败，需要延迟补注册</P>
     */
    private void registerMediaBtn(final boolean isReg) {
        Log.i(TAG, "registerMediaBtn(" + isReg + ")");
        if (mMediaBtnPresenter == null) {
            mMediaBtnPresenter = new MediaBtnPresenter(mContext);
        }

        if (isReg) {
            //MediaBtnReceiver.setListener(this);
            mMediaBtnPresenter.register(MediaBtnReceiver.class.getName());
        } else {
            mMediaBtnPresenter.unregister();
            //MediaBtnReceiver.setListener(null);
        }
    }

    /**
     * New media, parse and play.
     *
     * @param mediaUrl New media url.
     */
    private void parseAndPlayNewMedia(String mediaUrl) {
        Logs.i(TAG, "parseAndPlayNewMedia(" + mediaUrl + ")");
        if (mParseAndPlayTask != null) {
            mParseAndPlayTask.cancel(true);
            mParseAndPlayTask = null;
        }
        //EXEC task
        mParseAndPlayTask = new ParseAndPlayTask(this, mediaUrl);
        mParseAndPlayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class ParseAndPlayTask extends AsyncTask<Void, Void, ProAudio> {
        private WeakReference<PlayPresenter> mPresenter;
        private String mmPath;

        ParseAndPlayTask(PlayPresenter presenter, String path) {
            mPresenter = new WeakReference<>(presenter);
            mmPath = path;
        }

        @Override
        protected ProAudio doInBackground(Void... voids) {
            //Parse and add to list.
            try {
                Logs.i(TAG, "ParseAndPlayTask >> doInBackground()");
                final ProAudio media = new ProAudio(mmPath, null);
                ProAudio.parseMedia(mPresenter.get().mContext, media, null);
                return media;
            } catch (Exception e) {
                Log.i(TAG, "ParseAndPlayTask - doInBackground() >> e: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ProAudio audio) {
            super.onPostExecute(audio);
            Logs.i(TAG, "ParseAndPlayTask >> onPostExecute()");
            PlayPresenter presenter = mPresenter.get();
            if (presenter != null && audio != null) {
                // Reset temp audio list.
                presenter.mTempAudioList = null;
                // Create new audio list.
                presenter.mAudioList = new ArrayList<>();
                presenter.mAudioList.add(audio);
                // Play by url.
                presenter.playByUrlByUser(audio.getMediaUrl());
            }
        }
    }

    /**
     * Used to execute voice command.
     */
    private static class VoiceAssistantTask extends AsyncTask<Void, Void, List<ProAudio>> {
        //Used to call back.
        private WeakReference<PlayPresenter> mmWeakReferencePresenter;

        //Method type
        private int mmMethodType;
        final int TYPE_OPEN_AND_PLAY = 1; //打开音乐并播放
        final int TYPE_OPEN_AND_RANDOM_PLAY_ONE = 2;//打开音乐并随机放一首歌
        final int TYPE_OPEN_AND_PLAY_BY_ARTIST = 3;
        final int TYPE_OPEN_AND_PLAY_BY_MEDIA_URL = 4;
        final int TYPE_OPEN_AND_PLAY_BY_ARTIST_TITLE = 5;

        //Bind string, used  to query.
        private String mmPath, mmArtist, mmMediaName;
        private List<ProAudio> mmAudiosToPlay;

        /**
         * 打开并播放歌曲 或 打开并随机播放一首歌曲
         *
         * @param presenter          {@link PlayPresenter}
         * @param isOpenAndRandomOne true-打开并随机播放一首歌曲;false 打开并播放音乐
         */
        VoiceAssistantTask(PlayPresenter presenter, boolean isOpenAndRandomOne) {
            mmWeakReferencePresenter = new WeakReference<>(presenter);
            mmMethodType = isOpenAndRandomOne ? TYPE_OPEN_AND_RANDOM_PLAY_ONE : TYPE_OPEN_AND_PLAY;
            Logs.i(TAG, "VoiceAssistantTask -1->> mmMethodType: " + mmMethodType);
        }

        /**
         * 打开并播放指定媒体路径的音频
         *
         * @param presenter {@link PlayPresenter}
         * @param mediaUrl  Media url to play.
         */
        VoiceAssistantTask(PlayPresenter presenter, String mediaUrl) {
            mmWeakReferencePresenter = new WeakReference<>(presenter);
            mmMethodType = TYPE_OPEN_AND_PLAY_BY_MEDIA_URL;
            Logs.i(TAG, "VoiceAssistantTask -2->> mmMethodType: " + mmMethodType);
        }

        /**
         * 打开并播放指定[艺术家、媒体名称]的媒体
         *
         * @param presenter {@link PlayPresenter}
         * @param artist    Song artist.
         * @param title     Song name.
         */
        VoiceAssistantTask(PlayPresenter presenter, String artist, String title) {
            mmWeakReferencePresenter = new WeakReference<>(presenter);
            mmArtist = artist;
            mmMediaName = title;
            if (!TextUtils.isEmpty(artist) && !TextUtils.isEmpty(title)) {
                mmMethodType = TYPE_OPEN_AND_PLAY_BY_ARTIST_TITLE;
            } else if (!TextUtils.isEmpty(artist)) {
                mmMethodType = TYPE_OPEN_AND_PLAY_BY_ARTIST;
            } else {
                mmMethodType = TYPE_OPEN_AND_PLAY;
            }
            Logs.i(TAG, "VoiceAssistantTask -3->> mmMethodType: " + mmMethodType + ";mmArtist: " + mmArtist + ";mmMediaName: " + mmMediaName);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<ProAudio> doInBackground(Void... voids) {
            try {
                Logs.i(TAG, "VoiceAssistantTask >> doInBackground()");
                PlayPresenter playPresenter = mmWeakReferencePresenter.get();
                switch (mmMethodType) {
                    case TYPE_OPEN_AND_PLAY:
                    case TYPE_OPEN_AND_RANDOM_PLAY_ONE:
                        mmAudiosToPlay = null;
                        break;
                    case TYPE_OPEN_AND_PLAY_BY_MEDIA_URL:
                        MediaBase mediaBase = playPresenter.mDbManager.getMedia(mmPath);
                        if (mediaBase != null) {
                            ProAudio media = (ProAudio) mediaBase;
                            mmAudiosToPlay = new ArrayList<>();
                            mmAudiosToPlay.add(media);
                        }
                        break;
                    case TYPE_OPEN_AND_PLAY_BY_ARTIST:
                    case TYPE_OPEN_AND_PLAY_BY_ARTIST_TITLE:
                        mmAudiosToPlay = playPresenter.mDbManager.getListMusics(mmMediaName, mmArtist);
                        break;
                    default:
                        mmAudiosToPlay = null;
                        break;
                }

                //Get and return media list.
                return playPresenter.getAllMedias(FilterType.MEDIA_NAME, null);
            } catch (Exception e) {
                Log.i(TAG, "VoiceAssistantTask - doInBackground() >> e: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ProAudio> listMedias) {
            super.onPostExecute(listMedias);
            try {
                Logs.i(TAG, "VoiceAssistantTask >> onPostExecute()");
                PlayPresenter playPresenter = mmWeakReferencePresenter.get();
                //Set play list.
                playPresenter.mTempAudioList = null;
                playPresenter.mAudioList = listMedias;
                switch (mmMethodType) {
                    case TYPE_OPEN_AND_PLAY:
                        Logs.i(TAG, "VoiceAssistantTask - onPostExecute() >> TYPE_OPEN_AND_PLAY : playByUrlByUser()");
                        String lastMediaUrl = playPresenter.getLastMediaUrl();
                        Logs.i(TAG, "VoiceAssistantTask - onPostExecute() : mediaUrl - " + lastMediaUrl);
                        playPresenter.playByUrlByUser(lastMediaUrl);
                        break;
                    case TYPE_OPEN_AND_RANDOM_PLAY_ONE:
                        Logs.i(TAG, "VoiceAssistantTask - onPostExecute() >> TYPE_OPEN_AND_RANDOM_PLAY_ONE : playRandomByUser()");
                        playPresenter.playRandomByUser();
                        break;
                    case TYPE_OPEN_AND_PLAY_BY_ARTIST:
                        Logs.i(TAG, "VoiceAssistantTask - onPostExecute() >> TYPE_OPEN_AND_PLAY_BY_ARTIST : playByUrlByUser()");
                        String randomPathFromAudiosToPlay = getRandomPathFromAudiosToPlay(mmAudiosToPlay);
                        playPresenter.playByUrlByUser(randomPathFromAudiosToPlay);
                        break;
                    case TYPE_OPEN_AND_PLAY_BY_MEDIA_URL:
                        Logs.i(TAG, "VoiceAssistantTask - onPostExecute() >> TYPE_OPEN_AND_PLAY_BY_MEDIA_URL : playByUrlByUser()");
                        if (mmAudiosToPlay != null && mmAudiosToPlay.size() > 0) {
                            ProAudio media = mmAudiosToPlay.get(0);
                            playPresenter.playByUrlByUser(media.getMediaUrl());
                        }
                        break;
                    case TYPE_OPEN_AND_PLAY_BY_ARTIST_TITLE:
                        Logs.i(TAG, "VoiceAssistantTask - onPostExecute() >> TYPE_OPEN_AND_PLAY_BY_ARTIST_TITLE : playByUrlByUser()");
                        randomPathFromAudiosToPlay = getRandomPathFromAudiosToPlay(mmAudiosToPlay);
                        playPresenter.playByUrlByUser(randomPathFromAudiosToPlay);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                Log.i(TAG, "VoiceAssistantTask - onPostExecute() >> e: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private String getRandomPathFromAudiosToPlay(List<ProAudio> audiosToPlay) {
            //Get current media path, used to compare.
            PlayPresenter playPresenter = mmWeakReferencePresenter.get();
            String currMediaPath = playPresenter.getCurrMediaPath();

            //获取 当前播放的歌曲 在 查询到的列表 中的位置
            int posInNewList = -1;
            final int sizeOfNewList = audiosToPlay.size();
            for (int idx = 0; idx < sizeOfNewList; idx++) {
                ProAudio tmp = audiosToPlay.get(idx);
                if (TextUtils.equals(tmp.getMediaUrl(), currMediaPath)) {
                    posInNewList = idx;
                    break;
                }
            }

            //获取一个与当前播放位置不同的播放位置,这是为了每次获取的歌曲有变化
            int randomPos = Utils.getRandomNum(posInNewList, sizeOfNewList);
            return audiosToPlay.get(randomPos).getMediaUrl();
        }
    }

    @Override
    public void onGotMediaKey(KeyEvent event) {
        super.onGotMediaKey(event);
        boolean isAudioFocusGained = isAudioFocusGained();
        Logs.i(TAG, "onGotMediaKey >> [isAudioFocusGained : " + isAudioFocusGained);
        if (isAudioFocusGained) {
            int keyCode = event.getKeyCode();
            Log.i(TAG, "onGotMediaKey(" + keyCode + ")");
            switch (keyCode) {
                case KeyCodes.KEYCODE_MEDIA_PREVIOUS:
                    playPrevByUser();
                    break;
                case KeyCodes.KEYCODE_MEDIA_NEXT:
                    playNextByUser();
                    break;
                case KeyCodes.KEYCODE_MEDIA_PLAY_PAUSE:
                    playOrPauseByUser();
                    break;
                case KeyCodes.KEYCODE_MEDIA_PLAY:
                    resumeByUser();
                    break;
                case KeyCodes.KEYCODE_MEDIA_PAUSE:
                    pauseByUser();
                    break;
            }
        }
    }

    /**
     * Initialize and get [presenter of voice command].
     *
     * @return {@link IVoiceCmdPresenter}
     */
    private IVoiceCmdPresenter getVoiceCmdPresenter() {
        if (mVoiceCmdPresenter == null) {
            mVoiceCmdPresenter = new VoiceCmdPresenter(mContext, this);
        }
        return mVoiceCmdPresenter;
    }

    /**
     * Reset [presenter of voice command].
     */
    private void resetVoiceCmdPresenter() {
        Logs.i(TAG, "resetVoiceCmdPresenter()");
        if (mVoiceCmdPresenter != null) {
            mVoiceCmdPresenter.destroyVoiceCmd();
            mVoiceCmdPresenter = null;
        }
    }

    /**
     * @param status {@link android.egar.MediaStatus}
     *               //setenforce 0
     */
    private void updateStatusBar(int status) {
        Logs.i(TAG, "updateStatusBar(" + status + ")");
        if (mContext != null) {
            if (!isAudioFocusGained()) {
                SettingsSysUtil.setAudioState(mContext, MediaStatus.MEDIA_STATUS_STOP);
            } else if (status < 0) {
                SettingsSysUtil.setAudioState(mContext, isPlaying() ? MediaStatus.MEDIA_STATUS_PLAYING : MediaStatus.MEDIA_STATUS_PAUSE);
            } else {
                SettingsSysUtil.setAudioState(mContext, status);
            }
        }
    }
}
