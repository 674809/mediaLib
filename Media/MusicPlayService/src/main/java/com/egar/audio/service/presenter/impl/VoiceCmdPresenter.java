package com.egar.audio.service.presenter.impl;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.egar.audio.R;
import com.egar.audio.service.presenter.IVoiceCmdPresenter;

import juns.lib.android.utils.Logs;
import juns.lib.media.action.MediaActions;
import juns.lib.media.bean.MediaBase;

/**
 * Voice command presenter.
 * <p>Process voice cmd.</p>
 *
 * @author Jun.Wang
 */
public class VoiceCmdPresenter implements IVoiceCmdPresenter {
    private static final String TAG = "VoiceCmdPresenter";

    private Context mContext;
    private IVoiceCmdListener mVoiceCmdListener;

    private String mVoiceCommand;
    private int mType;
    private String[] mParams;

    public VoiceCmdPresenter(Context context, IVoiceCmdListener l) {
        mContext = context;
        mVoiceCmdListener = l;
    }

    @Override
    public void parseVoiceCmd(String action, Intent data) {
        // 非需要解析的命令，直接抛出命令
        if (!TextUtils.equals(MediaActions.MUSIC_OPEN_PLAY, action)) {
            setCmd(action, -1, null);
            return;
        }

        // 1==》打开并播放歌曲==>"我要听歌"
        // 2==》随机播放一首歌曲
        // 3==》播放指定歌手的歌曲。歌手的名称：请看artist参数值;
        //     歌曲路径看path参数值（如果path没值，播放器自己定义播放这个歌手的哪首歌曲）
        // 4==》播放指定歌名的歌曲。“我要听华阴老腔一声喊”
        //     歌曲路径看path参数值（必须要有path）;
        // 5==》播放指定歌手指定歌名的歌曲。歌手的名称:请看artist参数值（必须要有artist）;
        //     歌曲路径看path参数值（必须要有path）
        final String KEY_TYPE = "type";
        // 歌曲名 / 歌手 / 歌曲路径
        final String KEY_TITLE = "title", KEY_ARTIST = "artist", KEY_PATH = "path";

        //
        int type;
        String[] params = null;
        type = data.getIntExtra(KEY_TYPE, -1);
        if (type > 0) {
            String title = data.getStringExtra(KEY_TITLE);
            String artist = data.getStringExtra(KEY_ARTIST);
            String unknownArtist = mContext.getString(R.string.unknown_artist);
            Log.i(TAG, "artist[" + artist + "] // [" + unknownArtist + "]");
            if (TextUtils.equals(unknownArtist, artist)) {
                artist = MediaBase.UNKNOWN;
            }
            String path = data.getStringExtra(KEY_PATH);
            Log.i(TAG, "VoiceCmdController -onResume(Intent data)-"
                    + "\ntype: |" + type + "|"
                    + "\ntitle: |" + title + "|"
                    + "\nartist: |" + artist + "|"
                    + "\npath: |" + path + "|");

            //
            params = new String[3];
            params[0] = title;
            params[1] = artist;
            params[2] = path;
        }

        //Clear all parameters to ensure that they are only used once.
        data.removeExtra(KEY_TYPE);
        data.removeExtra(KEY_TITLE);
        data.removeExtra(KEY_ARTIST);
        data.removeExtra(KEY_PATH);

        //Callback
        setCmd(action, type, params);
    }

    /**
     * 解析语音命令
     *
     * @param cmd    广播字符串
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
    private void setCmd(String cmd, int type, String[] params) {
        mVoiceCommand = cmd;
        mType = type;
        mParams = params;
    }

    @Override
    public void processVoiceCmd() {
        Logs.i(TAG, "VoiceCmdPresenter -destroy()-");
        //Command check.
        if (mVoiceCommand != null && mVoiceCmdListener != null) {
            switch (mVoiceCommand) {
                case MediaActions.MUSIC_CLOSE:
                    mVoiceCmdListener.onVoiceCmdClose();
                    break;
                case MediaActions.MEDIA_PREV:
                    mVoiceCmdListener.onVoiceCmdPrev();
                    break;
                case MediaActions.MEDIA_NEXT:
                    mVoiceCmdListener.onVoiceCmdNext();
                    break;
                case MediaActions.MEDIA_PAUSE:
                    mVoiceCmdListener.onVoiceCmdPause();
                    break;
                case MediaActions.MUSIC_OPEN:
                case MediaActions.MEDIA_PLAY:
                    mVoiceCmdListener.onVoiceCmdPlay();
                    break;
                case MediaActions.MUSIC_OPEN_PLAY:
                    mVoiceCmdListener.onVoiceCmdPlay(mType, mParams);
                    mType = -1;
                    mParams = null;
                    break;
            }
        }
        mVoiceCommand = null;
    }

    @Override
    public boolean hasVoiceCmd() {
        return mVoiceCommand != null;
    }

    @Override
    public void destroyVoiceCmd() {
        Logs.i(TAG, "VoiceCmdPresenter -destroy()-");
        mVoiceCmdListener = null;
        mVoiceCommand = null;
    }
}
