package com.egar.audio.service.presenter;

import android.content.Intent;

import com.egar.audio.receiver.MediaBtnReceiver;
import com.egar.music.api.IAudioDataOpActions;
import com.egar.scanner.api.EgarApiScanner;

import juns.lib.media.play.IAudioPlayListener;
import juns.lib.media.play.IAudioPlayService;
import juns.lib.media.player.AudioPlayerFactory;
import juns.lib.media.scanner.IMediaScanListener;

/**
 * Audio playing presenter.
 *
 * @author Jun.Wang
 */
public abstract class IPlayPresenter extends IAudioPlayService.Stub implements IAudioPlayListener,
        IAudioDataOpActions,
        IMediaScanListener,
        EgarApiScanner.IEgarApiScanListener,
        AudioPlayerFactory.IAudioPlayerListener,
        IVolumeFadePresenter,
        IVolumeFadePresenter.IVolumeFadeListener,
        IAudioFocusPresenter.IAudioFocusListener,
        IVoiceCmdPresenter.IVoiceCmdListener,
        MediaBtnReceiver.MediaBtnListener {

    public abstract boolean isScanServiceConnected();

    /**
     * 通知接收到语音命令
     */
    public abstract void onGotVoiceCmd(String action, Intent data);

    /**
     * Start scanning.
     */
    public abstract void startScan();

    /**
     * Set position to play.
     *
     * @param position position to play.
     */
    public abstract void setPlayPosition(int position);

    /**
     * 设置左右声道声音比率
     *
     * @param leftVolume  [0f~1f]
     * @param rightVolume [0f~1f]
     */
    public abstract void setVolume(float leftVolume, float rightVolume);

    /**
     * Clear all the runnable/variables or others that need to be clean.
     */
    public abstract void destroy();

    /**
     * 保存播放的媒体信息
     * <p>这个方法应该在{{@link juns.lib.media.player.AudioPlayerFactory.IAudioPlayerListener#onPlayerProgressChanged(String, int, int)} 时调用，用来保存即时播放信息</p>
     *
     * @param mediaPath 当前媒体路径
     * @param progress  当前媒体进度
     */
    @SuppressWarnings("JavadocReference")
    public abstract void savePlayMediaInfo(String mediaPath, int progress);
}
