package juns.lib.media.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import juns.lib.android.utils.Logs;
import juns.lib.media.action.IAudioPlayer;
import juns.lib.media.flags.PlayState;
import juns.lib.media.player.AudioPlayerFactory.IAudioPlayerListener;

/**
 * Native {@link MediaPlayer} implement class.
 *
 * @author Jun.Wang
 */
public class NativeAudioPlayer implements IAudioPlayer {
    // LOG TAG
    private final String TAG = "NativeAudioPlayer";

    /**
     * Handler used to loop response current position of media.
     */
    private Handler mHandler;

    /**
     * Context Object
     */
    private Context mContext;

    /**
     * 当前正在播放的媒体路径
     */
    private String mMediaPath;

    /**
     * 是否是异步加载
     */
    private boolean mIsPrepareAsync = false;
    /**
     * prepareAsync，在加载过程中有可能会提前调用onCompletion/onError,这个标记就是用来区分是否已经prepare结束了
     */
    private boolean mIsPreparing = false;

    /**
     * MediaPlayer的Seek是异步的，这个标记是用来判断当前Seek状态的
     */
    private boolean mIsMediaSeeking = false;

    /**
     * Android MediaPlayer
     */
    private static MediaPlayer mMediaPlayer;

    /**
     * Player Listener
     */
    private IAudioPlayerListener mAudioPlayerListener;

    /**
     * Create Music Player - MediaPlayer
     */
    public NativeAudioPlayer(Context cxt, IAudioPlayerListener l) {
        this.mContext = cxt;
        this.mAudioPlayerListener = l;
    }

    /**
     * Create Media Player Object
     */
    private void createMediaPlayer(Context cxt, String mediaPath) {
        try {
            //Create and check.
            mMediaPlayer = MediaPlayer.create(cxt, Uri.parse(mediaPath));
            if (mMediaPlayer == null) {
                return;
            }

            //Set event.
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(new MediaPlayerOnPrepared());
            mMediaPlayer.setOnCompletionListener(new MediaPlayerOnCompletion());
            mMediaPlayer.setOnInfoListener(new MediaPlayerOnInfo());
            mMediaPlayer.setOnErrorListener(new MediaPlayerOnError());
            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayerOnSeekComplete());
        } catch (Exception e) {
            Logs.i(TAG, TAG + "createMediaPlayer() >> e: " + e.getMessage());
            notifyPlayState(PlayState.ERROR_PLAYER_INIT);
        }
    }

    private class MediaPlayerOnPrepared implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Logs.i(TAG, "MediaPlayerOnPrepared >> onPrepared()");
            notifyPlayState(PlayState.PREPARED);
            mIsPreparing = false;
            if (mIsPrepareAsync) {
                mIsPrepareAsync = false;
                playMedia();
            }
        }
    }

    private class MediaPlayerOnCompletion implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Logs.i(TAG, "MediaPlayerOnPrepared >> onCompletion()");
            if (!mIsPreparing) {
                notifyPlayState(PlayState.COMPLETE);
            }
        }
    }

    private class MediaPlayerOnInfo implements MediaPlayer.OnInfoListener {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_UNKNOWN:
                    Log.i(TAG, "onInfo - 未知的信息");
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    Log.i(TAG, "onInfo - 视频过于复杂解码太慢");
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.i(TAG, "onInfo - 开始渲染第一帧");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.i(TAG, "onInfo - 暂停播放开始缓冲更多数据");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.i(TAG, "onInfo - 缓冲了足够的数据重新开始播放");
                    break;
                case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    Log.i(TAG, "onInfo - 错误交叉");
                    break;
                case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    Log.i(TAG, "onInfo - 媒体不能够搜索");
                    break;
                case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    Log.i(TAG, "onInfo - 一组新的元数据用");
                    break;
                case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    Log.i(TAG, "onInfo - 读取字幕使用时间过长");
                    break;
            }
            return false;
        }
    }

    private class MediaPlayerOnError implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            // Process Error
            boolean isProcessError = true;
            switch (what) {
                // 未发现该问题有何用处，暂不处理该错误
                case -38:
                    isProcessError = false;
                    break;
                // Player Died Error
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mMediaPlayer.reset();
                    break;
            }
            // 通知监听器处理ERROR
            if (isProcessError) {
                notifyPlayState(PlayState.ERROR);
            }

            // LOG
            Logs.i(TAG, "MediaPlayerOnError -> onError(mp," + what + "," + extra + ")");
            MediaUtils.printError(mp, what, extra);
            return false;
        }
    }

    private class MediaPlayerOnSeekComplete implements MediaPlayer.OnSeekCompleteListener {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (!mIsPreparing) {
                mIsMediaSeeking = true;
                notifyPlayState(PlayState.SEEK_COMPLETED);
            }
        }
    }

    @Override
    public void playMedia(String mediaUrl) {
        Logs.i(TAG, "^^ play(" + mediaUrl + ") ^^");
        try {
            startProgressTimer(false);
            this.mIsPrepareAsync = true;
            this.mIsPreparing = true;
            this.mMediaPath = mediaUrl;
            notifyPlayState(PlayState.REFRESH_UI);
            if (mMediaPlayer == null) {
                createMediaPlayer(mContext, mediaUrl);
            } else {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mediaUrl);
                mMediaPlayer.prepareAsync();
            }
        } catch (Exception e) {
            notifyPlayState(PlayState.ERROR);
            Log.i(TAG, "playMedia(mediaUrl) >> [e: " + e.getMessage() + " ]");
        }
    }

    @Override
    public void playMedia() {
        Logs.i(TAG, "^^ playMedia() ^^ [mMediaPlayer:" + mMediaPlayer + "]");
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            Log.i(TAG, "mMediaPath : " + mMediaPath + "   isPlaying:" + mMediaPlayer.isPlaying());
            startProgressTimer(true);
            notifyPlayState(PlayState.PLAY);
        }
    }

    @Override
    public void pauseMedia() {
        Logs.i(TAG, "^^ pauseMedia() ^^");
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            startProgressTimer(false);
            notifyPlayState(PlayState.PAUSE);
        }
    }

    @Override
    public void releaseMedia() {
        Logs.i(TAG, "^^ releaseMedia() ^^");
        // Reset progress timer.
        startProgressTimer(false);
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();
            } catch (Exception e) {
                Logs.i(TAG, "releaseMedia() - reset >> e: " + e.getMessage());
            }

            try {
                mMediaPlayer.stop();
            } catch (Exception e) {
                Logs.i(TAG, "releaseMedia() - stop >> e: " + e.getMessage());
            }

            try {
                mMediaPlayer.release();
            } catch (Exception e) {
                Logs.i(TAG, "releaseMedia() - release >> e: " + e.getMessage());
            }

            // Notify release.
            notifyPlayState(PlayState.RELEASE);
            // Reset player and listener.
            mMediaPlayer = null;
            mAudioPlayerListener = null;
        }
    }

    @Override
    public boolean isMediaPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public void setMediaPath(String path) {
        this.mMediaPath = path;
    }

    @Override
    public String getMediaPath() {
        return this.mMediaPath;
    }

    @Override
    public int getMediaTime() {
        if (mMediaPlayer != null) {
            try {
                return mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                Logs.i(TAG, "getMediaTime() >> [e: " + e.getMessage());
            }
        }
        return 0;
    }

    @Override
    public int getMediaDuration() {
        if (mMediaPlayer != null) {
            try {
                return mMediaPlayer.getDuration();
            } catch (Exception e) {
                Logs.i(TAG, "getMediaTime() >> [e: " + e.getMessage());
            }
        }
        return 0;
    }

    @Override
    public void seekMediaTo(int millisecond) {
        if (mMediaPlayer != null) {
            if (millisecond >= (getMediaDuration() - 1000)) {
                if (!mIsPreparing) {
                    notifyPlayState(PlayState.COMPLETE);
                }
            } else {
                mIsMediaSeeking = true;
                mMediaPlayer.seekTo(millisecond);
            }
        }
    }

    @Override
    public boolean isMediaSeeking() {
        return mIsMediaSeeking;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null) {
            //Correct value.
            if (leftVolume < 0.0f) {
                leftVolume = 0.0f;
            }
            if (leftVolume > 1.0f) {
                leftVolume = 1.0f;
            }
            //Correct value.
            if (rightVolume < 0.0f) {
                rightVolume = 0.0f;
            }
            if (rightVolume > 1.0f) {
                rightVolume = 1.0f;
            }
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    /**
     * EXEC Start or Cancel Progress Timer
     */
    private void startProgressTimer(boolean isStart) {
        Logs.i(TAG, "-- startProgressTimer(" + isStart + ") --");
        if (isStart) {
            //通知进度
            notifyProgress(getMediaPath(), getMediaTime(), getMediaDuration());

            //执行下一次进度循环
            try {
                //Prepare looper.
                if (mHandler == null) {
                    try {
                        Looper.prepare();//Only could be executed once.
                    } catch (Exception e) {
                        Logs.i(TAG, "startProgressTimer(true) >> [e: " + e.getMessage());
                    }
                    mHandler = new Handler();
                }

                //Delay loop
                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startProgressTimer(true);
                    }
                }, 1000);
            } catch (Exception e) {
                Logs.i(TAG, "startProgressTimer(true) >> e: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Notify Play state
     */
    private void notifyPlayState(int playState) {
        if (mAudioPlayerListener != null) {
            mAudioPlayerListener.onPlayerStateChanged(playState);
        }
    }

    /**
     * Notify progress
     *
     * @param path     - Media path
     * @param time     - Media time in duration
     * @param duration - Media duration
     */
    private void notifyProgress(String path, int time, int duration) {
        Logs.i(TAG, "notifyProgress(" + path + "," + time + "," + duration + ")");
        if (mAudioPlayerListener != null) {
            mAudioPlayerListener.onPlayerProgressChanged(path, time, duration);
        }
    }
}