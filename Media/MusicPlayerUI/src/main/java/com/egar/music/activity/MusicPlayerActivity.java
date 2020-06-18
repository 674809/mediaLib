package com.egar.music.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.egar.music.R;
import com.egar.music.activity.base.BaseUiActivity;
import com.egar.music.api.EgarApiMusic;
import com.egar.music.utils.AudioUtils;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.java.utils.date.DateFormatUtil;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.flags.KeyCodes;
import juns.lib.media.flags.MediaCollectState;
import juns.lib.media.flags.PlayMode;
import juns.lib.media.flags.PlayModeSupportType;
import juns.lib.media.flags.PlayState;
import xskin.utils.SkinUtil;

/**
 * Music player activity.
 *
 * @author Jun.Wang
 */
public class MusicPlayerActivity extends BaseUiActivity {
    //TAG
    private static final String TAG = "MusicPlayerActivity";

    //==========Widgets in this Activity==========
    private TextView tvName, tvArtist, tvAlbum;
    private ImageView ivMusicCover;
    private TextView tvStartTime, tvEndTime;
    private RelativeLayout layoutSeekbar;
    private SeekBar seekBar;
    private ImageView ivPlayPre, ivPlay, ivPlayNext, ivCollect, ivPlayModeSet, ivList,bt_finish;

    //==========Variables in this Activity==========
    private Context mContext;

    //拖动条监听事件
    private SeekBarOnChange mSeekBarOnChange;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logs.i("TIME_COL", "-4-" + System.currentTimeMillis());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Logs.i("TIME_COL", "-5-" + System.currentTimeMillis());
        Logs.i(TAG, "onCreate()");
        addToStack(this);
        init();
    }

    private void init() {
        // Initialize Variables
        mContext = this;

        // -- Widgets --
        tvName = (TextView) findViewById(R.id.tv_name);
        tvName.setText("");
        tvName.setSelected(false);
        tvName.setOnClickListener(mViewOnClick);

        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvArtist.setText("");
        tvArtist.setOnClickListener(mViewOnClick);

        tvAlbum = (TextView) findViewById(R.id.tv_album);
        tvAlbum.setText("");
        tvAlbum.setOnClickListener(mViewOnClick);

        layoutSeekbar = (RelativeLayout) findViewById(R.id.rl_seek_bar);
        tvStartTime = (TextView) findViewById(R.id.tv_play_start_time);
        tvEndTime = (TextView) findViewById(R.id.tv_play_end_time);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setEnabled(true);
        seekBar.setOnSeekBarChangeListener((mSeekBarOnChange = new SeekBarOnChange()));

        ivMusicCover = (ImageView) findViewById(R.id.iv_music_cover);
        ivMusicCover.setOnClickListener(mViewOnClick);

        bt_finish = (ImageView) findViewById(R.id.bt_finish);
        bt_finish.setOnClickListener(mViewOnClick);

        ivPlayPre = (ImageView) findViewById(R.id.iv_play_pre);
        ivPlayPre.setOnClickListener(mViewOnClick);

        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivPlay.setOnClickListener(mViewOnClick);

        ivPlayNext = (ImageView) findViewById(R.id.iv_play_next);
        ivPlayNext.setOnClickListener(mViewOnClick);

        ivCollect = (ImageView) findViewById(R.id.v_favor);
        ivCollect.setOnClickListener(mViewOnClick);

        ivPlayModeSet = (ImageView) findViewById(R.id.iv_play_mode_set);
        ivPlayModeSet.setOnClickListener(mViewOnClick);

        ivList = (ImageView) findViewById(R.id.v_list);
        ivList.setOnClickListener(mViewOnClick);

        //
        tvName.post(new Runnable() {
            @Override
            public void run() {
                bindPlayService(true);
            }
        });
    }

    @Override
    protected void onResume() {
        Logs.i(TAG, "onResume()");
        Logs.i("TIME_COL", "-6-" + System.currentTimeMillis());
        super.onResume();
        //Register Audio focus.
        boolean isPlayServiceConnected = isPlayServiceConnected();
        Log.i(TAG, "onResume() - isAudioServiceConned:" + isPlayServiceConnected + "-");
        //Register Audio focus when service is bound.
        if (isPlayServiceConnected) {
            focusPlayer();//Focus player page.
        }
    }

    @Override
    public void focusPlayer() {
        Logs.i(TAG, "focusPlayer()");
        super.focusPlayer(); //Focus player page
    }

    @Override
    public void onAudioPlayServiceConnected() {
        Logs.i(TAG, "onAudioPlayServiceConnected()");
        super.onAudioPlayServiceConnected();
        //Focus page.
        focusPlayer();
        // Initialize play mode.
        onPlayModeChanged(getPlayMode());
        // Load local medias.
        loadLocalMedias();
    }

    /**
     * Load current media information.
     */
    private void loadLocalMedias() {
        Log.i(TAG, "loadLocalMedias()");
        refreshCurrMediaInfo();
        refreshUIOfPlayBtn(isPlaying() ? 1 : 2);
        refreshFrameInfo(1, 0, 0);
    }

    /**
     * View Click Event
     */
    private View.OnClickListener mViewOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == ivMusicCover) {
                //toggleSkin();

                //Mode
            } else if (v == ivPlayModeSet) {
                switchPlayMode(PlayModeSupportType.NO_ORDER);

                //Play
            } else if (v == ivPlayPre) {
                playPrevByUser();
            } else if (v == ivPlay) {
                playOrPauseByUser();
            } else if (v == ivPlayNext) {
                playNextByUser();

                //Collect
            } else if (v == ivCollect) {
                collect();
            } else if (v == ivList) {
                execFinish("PLAYER_FINISH_ON_CLICK_LIST", null);

                //Click music information
            } else if (v == tvName) {
                ProAudio media = getCurrMedia();
                if (media != null) {
                    execFinish("PLAYER_FINISH_ON_CLICK_TITLE", new String[]{media.getTitle()});
                }
            } else if (v == tvArtist) {
                ProAudio media = getCurrMedia();
                if (media != null) {
                    execFinish("PLAYER_FINISH_ON_CLICK_ARTIST", new String[]{media.getArtist()});
                }
            } else if (v == tvAlbum) {
                ProAudio media = getCurrMedia();
                if (media != null) {
                    execFinish("PLAYER_FINISH_ON_CLICK_ALBUM", new String[]{media.getAlbum()});
                }

            }else if(v == bt_finish){
               //clearActivity();
            }
        }
    };

    void collect() {
        ProAudio media = getCurrMedia();
        if (media != null) {
            switch (media.getCollected()) {
                case MediaCollectState.COLLECTED:
                    media.setCollected(0);
                    SkinUtil.instance().setImageDrawable(this, ivCollect, R.drawable.btn_op_favor_selector);
                    break;
                case MediaCollectState.UN_COLLECTED:
                    media.setCollected(1);
                    SkinUtil.instance().setImageDrawable(this, ivCollect, R.drawable.btn_op_favored_selector);
                    break;
            }

            //
            int posToCollect = getCurrPos();
            int res = updateMediaCollect(posToCollect, media);
            if (res > 0) {
                publishEbCollect(posToCollect, media);
            }
        }
    }

    /**
     * Execute finish methods.
     */
    private void execFinish(String flag, String[] values) {
        //
        Intent data = new Intent();
        data.putExtra("flag", flag);
        if (values != null) {
            data.putExtra("values", values);
        }
        setResult(0, data);
        finish();
    }

    @Override
    public void onPlayModeChanged(int newPlayModeValue) {
        super.onPlayModeChanged(newPlayModeValue);
        Logs.i(TAG, "onPlayModeChange(" + PlayMode.desc(newPlayModeValue) + ")");
        switch (newPlayModeValue) {
            case PlayMode.LOOP:
                SkinUtil.instance().setImageDrawable(this, ivPlayModeSet, R.drawable.btn_op_mode_loop_selector);
                break;
            case PlayMode.SINGLE:
                SkinUtil.instance().setImageDrawable(this, ivPlayModeSet, R.drawable.btn_op_mode_oneloop_selector);
                break;
            case PlayMode.RANDOM:
                SkinUtil.instance().setImageDrawable(this, ivPlayModeSet, R.drawable.btn_op_mode_random_selector);
                break;
        }
    }

    @Override
    public void onGotKey(int keyCode) {
        switch (keyCode) {
            case KeyCodes.KEYCODE_MEDIA_PREVIOUS:
            case KeyCodes.KEYCODE_MEDIA_NEXT:
            case KeyCodes.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyCodes.KEYCODE_MEDIA_PLAY:
            case KeyCodes.KEYCODE_MEDIA_PAUSE:
                break;
            case KeyCodes.KEYCODE_BACK:
                //onBackPressed();
                break;
            case KeyCodes.KEYCODE_ENTER:
                playOrPauseByUser();
                break;
            case KeyCodes.KEYCODE_DPAD_LEFT:
                execFinish("PLAYER_FINISH_ON_DPAD_LEFT", null);
                break;
            case KeyCodes.KEYCODE_DPAD_RIGHT:
                execFinish("PLAYER_FINISH_ON_DPAD_RIGHT", null);
                break;
        }
    }

    @Override
    public void onPlayStateChanged(final int playStateValue) {
        if (!isActActive()) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Process play state
                switch (playStateValue) {
                    case PlayState.REFRESH_UI:
                        refreshCurrMediaInfo();
                        break;
                    case PlayState.PLAY:
                        refreshUIOfPlayBtn(1);
                        break;
                    case PlayState.PREPARED:
                        if (EgarApiMusic.isPlayEnable(mContext)) {
                            refreshUIOfPlayBtn(1);
                            refreshFrameInfo(0, 0, 0);
                        } else {
                            refreshUIOfPlayBtn(2);
                        }
                        break;
                    case PlayState.SEEK_COMPLETED:
                        break;
                    case PlayState.PAUSE:
                        refreshUIOfPlayBtn(2);
                        break;
                    //暂停音频的情况下,将进度条拖动到最后也有可能触发该事件,所以此时不能重置时间
                    case PlayState.COMPLETE:
                    case PlayState.RELEASE:
                        refreshUIOfPlayBtn(2);
                        break;
                    case PlayState.ERROR:
                        refreshUIOfPlayBtn(2);

                        //Toast error message.
                        ProAudio mediaWithError = getCurrMedia();
                        if (mediaWithError != null) {
                            Logs.i(TAG, "onNotifyPlayState$Error :: " + mediaWithError.getMediaUrl());
                            AudioUtils.toastPlayError(mContext, mediaWithError.getTitle());
                        }
                        break;
                }
            }
        });
    }

    private void refreshCurrMediaInfo() {
        Log.i(TAG, "refreshCurrMediaInfo()");
        final ProAudio media = getCurrMedia();
        if (media != null) {
            setMediaInformation(media);
        }
    }

    private void setMediaInformation(ProAudio media) {
        // Media Cover
        AudioUtils.setMediaCover(ivMusicCover, media);
        // Title
        String strTitle = AudioUtils.getMediaTitle(mContext, -1, media, true);
        if (ProAudio.UNKNOWN.equals(strTitle)) {
            tvName.setText(R.string.unknown_title);
        } else {
            tvName.setText(strTitle);
        }
        //Artist
        String strArtist = media.getArtist();
        if (ProAudio.UNKNOWN.equals(strArtist) || EmptyUtil.isEmpty(strArtist)) {
            tvArtist.setText(R.string.unknown_artist);
        } else {
            tvArtist.setText(strArtist);
        }
        //Album
        String strAlbum = media.getAlbum();
        if (ProAudio.UNKNOWN.equals(strAlbum) || EmptyUtil.isEmpty(strAlbum)) {
            tvAlbum.setText(R.string.unknown_album);
        } else {
            tvAlbum.setText(strAlbum);
        }

        //Collect status
        switch (media.getCollected()) {
            case MediaCollectState.UN_COLLECTED:
                SkinUtil.instance().setImageDrawable(this, ivCollect, R.drawable.btn_op_favor_selector);
                break;
            case MediaCollectState.COLLECTED:
                SkinUtil.instance().setImageDrawable(this, ivCollect, R.drawable.btn_op_favored_selector);
                break;
        }
    }

    private void refreshUIOfPlayBtn(int flag) {
        switch (flag) {
            case 1:
                SkinUtil.instance().setImageDrawable(this, ivPlay, R.drawable.btn_op_pause_selector);
                break;
            case 2:
                SkinUtil.instance().setImageDrawable(this, ivPlay, R.drawable.btn_op_play_selector);
                break;
        }
    }

    /**
     * Refresh seekBar
     *
     * @param flag <p>0-Initialize;</p>
     *             <p>1-Refresh current progress</p>
     *             <p>2-Update progress on calling {@link #onPlayProgressChanged(String, int, int)}</p>
     */
    @SuppressWarnings("JavadocReference")
    private void refreshFrameInfo(int flag, int paramProgress, int paramDuration) {
        switch (flag) {
            //Set on player prepared
            case 0:
                Log.i(TAG, "refreshFrameInfo[0] - Reset.");
                //Duration
                int duration = (int) getDuration();
                seekBar.setMax(duration);
                tvEndTime.setText(DateFormatUtil.getFormatHHmmss(duration));
                //Progress
                seekBar.setProgress(0);
                tvStartTime.setText(DateFormatUtil.getFormatHHmmss(0));
                break;

            //Refresh current progress/duration
            case 1:
                boolean isPlaying = isPlaying();
                if (isPlaying) {
                    Log.i(TAG, "refreshSeekBar[1] - audio is playing now.");
                    //Duration
                    duration = (int) getDuration();
                    seekBar.setMax(duration);
                    tvEndTime.setText(DateFormatUtil.getFormatHHmmss(duration));
                    //Progress
                    int currProgress = (int) getProgress();
                    if (currProgress <= duration) {
                        seekBar.setProgress(currProgress);
                        tvStartTime.setText(DateFormatUtil.getFormatHHmmss(currProgress));
                    } else {
                        seekBar.setProgress(duration);
                        tvStartTime.setText(DateFormatUtil.getFormatHHmmss(duration));
                    }
                } else {
                    Log.i(TAG, "refreshSeekBar[1] - audio is paused now.");
                    ProAudio currMedia = getCurrMedia();
                    if (currMedia == null) {
                        //Duration
                        seekBar.setMax(0);
                        tvEndTime.setText(DateFormatUtil.getFormatHHmmss(0));
                        //Progress
                        seekBar.setProgress(0);
                        tvStartTime.setText(DateFormatUtil.getFormatHHmmss(0));
                    } else {
                        //Duration
                        duration = (int) (currMedia.getDuration() > 0 ? currMedia.getDuration() : getDuration());
                        seekBar.setMax(duration);
                        tvEndTime.setText(DateFormatUtil.getFormatHHmmss(duration));
                        //Progress
                        int currProgress = seekBar.getProgress();
                        if (currProgress <= duration) {
                            seekBar.setProgress(currProgress);
                            tvStartTime.setText(DateFormatUtil.getFormatHHmmss(currProgress));
                        } else {
                            seekBar.setProgress(duration);
                            tvStartTime.setText(DateFormatUtil.getFormatHHmmss(duration));
                        }
                    }
                }
                break;

            case 2:
                //Set SeekBar-Progress
                if (!mSeekBarOnChange.isTrackingTouch()) {
                    Logs.debugI(TAG, "refreshSeekBar[2] - update progress.");
                    //Duration
                    if (paramProgress > seekBar.getMax()) {
                        paramDuration = paramProgress;
                    }
                    seekBar.setMax(paramDuration);
                    tvEndTime.setText(DateFormatUtil.getFormatHHmmss(paramDuration));
                    //Progress
                    seekBar.setProgress(paramProgress);
                    tvStartTime.setText(DateFormatUtil.getFormatHHmmss(paramProgress));
                }
                break;
        }
    }

    @Override
    public void onPlayProgressChanged(String mediaPath, final int progress, final int duration) {
        super.onPlayProgressChanged(mediaPath, progress, duration);
        Logs.debugI(TAG, "onPlayProgressChanged(" + mediaPath + "," + progress + "," + duration + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshFrameInfo(2, progress, duration);
            }
        });
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        //        super.overridePendingTransition(enterAnim, exitAnim);
        super.overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG, "finish()");
        clearActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        clearActivity();
    }

    private void clearActivity() {
        Log.i(TAG, "clearActivity()");
        //Remove from stack.
        removeFromStack(this);
        //Unbind play service.
        bindPlayService(false);
    }

    private final class SeekBarOnChange implements SeekBar.OnSeekBarChangeListener {

        int mmProgress;
        boolean mmIsTracking = false;

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "SeekBarOnChange - onStartTrackingTouch");
            mmIsTracking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "SeekBarOnChange - onStopTrackingTouch");
            if (mmIsTracking) {
                mmIsTracking = false;
                seekTo(mmProgress);

                //Refresh UI
                tvStartTime.setText(DateFormatUtil.getFormatHHmmss(mmProgress));
                tvEndTime.setText(DateFormatUtil.getFormatHHmmss(seekBar.getMax()));
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Logs.debugI(TAG, "SeekBarOnChange - onProgressChanged(SeekBar," + progress + "," + fromUser + ")");
            if (fromUser) {
                mmProgress = progress;
            }
        }

        boolean isTrackingTouch() {
            return mmIsTracking;
        }
    }
}
