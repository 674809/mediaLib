package com.egar.audio.service.presenter;

import android.content.Intent;

/**
 * 语音命令处理逻辑行为定义该
 *
 * @author Jun.Wang
 */
public interface IVoiceCmdPresenter {

    /**
     * Voice command response listener.
     */
    public interface IVoiceCmdListener {
        /**
         * Close music.
         */
        void onVoiceCmdClose();

        /**
         * Voice command - PREV
         */
        void onVoiceCmdPrev();

        /**
         * Voice command - NEXT
         */
        void onVoiceCmdNext();

        /**
         * Voice command - PAUSE
         */
        void onVoiceCmdPause();

        /**
         * Voice command - PLAY
         */
        void onVoiceCmdPlay();

        /**
         * 打开并播放指定音乐
         * <p>执行到此处的时，一定已经获得到了声音焦点.</p>
         *
         * @param type   语音命令类型
         *               <p>1==》打开并播放歌曲==>"我要听歌";</p>
         *               <p>2==》随机播放一首歌曲;</p>
         *               <p>3==》播放指定歌手的歌曲。歌手的名称：请看artist参数值;
         *               歌曲路径看path参数值（如果path没值，播放器自己定义播放这个歌手的哪首歌曲）;</p>
         *               <p>4==》播放指定歌名的歌曲。“我要听华阴老腔一声喊”
         *               歌曲路径看path参数值（必须要有path）;</p>
         *               <p>5==》播放指定歌手指定歌名的歌曲。歌手的名称:请看artist参数值（必须要有artist）;
         *               歌曲路径看path参数值（必须要有path）.</p>
         *               <p></p>
         * @param params [0] title; [1] artist; [2] path.
         */
        void onVoiceCmdPlay(int type, String[] params);
    }

    /**
     * 解析语音命令
     *
     * @param action BroadcastReceiver action.
     * @param data   BroadcastReceiver data.
     */
    void parseVoiceCmd(String action, Intent data);

    /**
     * 处理语音命令
     */
    void processVoiceCmd();

    /**
     * Check if has voice command.
     */
    boolean hasVoiceCmd();

    /**
     * 销毁语音命令
     */
    void destroyVoiceCmd();
}
