package juns.lib.media.player;

import android.content.Context;

import juns.lib.media.action.IAudioPlayer;

public class AudioPlayerFactory {

    /**
     * {@link IAudioPlayer} Object
     */
    private static IAudioPlayer mPlayer;

    /**
     * Player Type Flag
     */
    private int mPlayerType = PlayerType.MEDIA_PLAYER;

    public interface PlayerType {
        int VLC_PLAYER = 1;
        int MEDIA_PLAYER = 2;
    }

    /**
     * Audio player action.
     */
    public interface IAudioPlayerListener {
        /**
         * PlayState - 通知播放器状态
         *
         * @param playState 播放器状态
         */
        void onPlayerStateChanged(int playState);

        /**
         * PlayProgress - 进度改变回调
         *
         * @param mediaPath 正在播放的媒体路径
         * @param progress  当前进度
         * @param duration  总时长
         */
        void onPlayerProgressChanged(String mediaPath, int progress, int duration);
    }

    /**
     * Private constructor
     */
    private AudioPlayerFactory() {
    }

    private static class SingletonHolder {
        private static final AudioPlayerFactory INSTANCE = new AudioPlayerFactory();
    }

    public static AudioPlayerFactory instance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialize player
     *
     * @param playerType : {@link PlayerType}
     */
    public void init(int playerType) {
        mPlayerType = playerType;
    }

    /**
     * Create
     */
    public IAudioPlayer create(Context cxt, String mediaPath, IAudioPlayerListener l) {
        switch (mPlayerType) {
            case PlayerType.VLC_PLAYER:
//                mPlayer = new AudioVlcPlayer(cxt, mediaPath, delegate);
                break;
            case PlayerType.MEDIA_PLAYER:
                mPlayer = new NativeAudioPlayer(cxt, l);
                break;
        }
        return mPlayer;
    }

    /**
     * Destroy
     */
    public void destroy() {
        if (mPlayer != null) {
            mPlayer.releaseMedia();
            mPlayer = null;
        }
    }
}
