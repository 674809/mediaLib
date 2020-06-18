package com.egar.music.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.egar.music.R;
import com.egar.music.activity.base.BaseUiActivity;
import com.egar.music.api.EgarApiMusic;
import com.egar.music.engine.EventBusDelegate;
import com.egar.music.fragment.AudioAlbumFragment;
import com.egar.music.fragment.AudioArtistFragment;
import com.egar.music.fragment.AudioCollectFragment;
import com.egar.music.fragment.AudioFolderFragment;
import com.egar.music.fragment.AudioTitleFragment;
import com.egar.music.fragment.BaseAudioFragment;
import com.egar.scanner.api.engine.Configs;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.FragUtil;
import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.flags.KeyCodes;
import juns.lib.media.flags.MediaScanState;
import juns.lib.media.flags.PlayState;
import juns.lib.media.utils.SDCardUtils;
import xskin.utils.SkinUtil;

/**
 * Music list activity.
 *
 * @author Jun.Wang
 */
public class MusicListActivity extends BaseUiActivity implements EventBusDelegate.EventBusCallback {
    //TAG
    private static final String TAG = "MusicListActivity";

    //==========Variables in this Activity==========
    //Activity context
    private Context mContext;

    //
    private BaseAudioFragment mFragCurrent;
    private View[] vItems = new View[5];
    private final int CATEGORY_COLLECT = 0;
    private final int CATEGORY_FOLDER = 1;
    private final int CATEGORY_TITLE = 2;
    private final int CATEGORY_ARTIST = 3;
    private final int CATEGORY_ALBUM = 4;

    //==========Widgets in this Activity==========
    //垃圾聚焦控件: 该控件主要是在其他空间不需要聚焦时，将聚焦转移到此。
    private View mVRubbishFocus;

    // ImageView with rhythm animation.
    private ImageView mIvRhythmAnim;

    //Handler
    //自动打开播放器
    private MyHandler mHandler = new MyHandler();

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logs.i(TAG, "onNewIntent(intent)");
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        Logs.i(TAG, "onCreate()");
        addToStack(this);
        addEbCallback(this);

        //
        init();
    }

    private void init() {
        Logs.i(TAG, "init()");
        // -- Variables --
        mContext = this;

        // -- Widgets --
        mVRubbishFocus = findViewById(R.id.v_rubbish_focus);

        // Item0
        vItems[CATEGORY_COLLECT] = findViewById(R.id.v_my_favorite);
        vItems[CATEGORY_COLLECT].setOnClickListener(mViewOnClick);
        // Item1
        vItems[CATEGORY_FOLDER] = findViewById(R.id.v_folder);
        vItems[CATEGORY_FOLDER].setOnClickListener(mViewOnClick);
        // Item2
        vItems[CATEGORY_TITLE] = findViewById(R.id.v_music_name);
        vItems[CATEGORY_TITLE].setOnClickListener(mViewOnClick);
        // Item3
        vItems[CATEGORY_ARTIST] = findViewById(R.id.v_artist);
        vItems[CATEGORY_ARTIST].setOnClickListener(mViewOnClick);
        // Item4
        vItems[CATEGORY_ALBUM] = findViewById(R.id.v_album);
        vItems[CATEGORY_ALBUM].setOnClickListener(mViewOnClick);

        // animation View
        mIvRhythmAnim = (ImageView) findViewById(R.id.v_rate);
        mIvRhythmAnim.setOnClickListener(mViewOnClick);

        //BIND play service
        mVRubbishFocus.post(new Runnable() {
            @Override
            public void run() {
                bindPlayService(true);
                requestPermission();
            }
        });
    }

    private void requestPermission() {
        if (!Configs.IS_OFFICIAL_VERSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "");
                } else {
                    String[] requestPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    this.requestPermissions(requestPermissions, 0);
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Logs.i(TAG, "onWindowFocusChanged(" + hasFocus + ")");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Register Audio focus.
        boolean isPlayServiceConnected = isPlayServiceConnected();
        Logs.i(TAG, "onResume() >> [isPlayServiceConnected: " + isPlayServiceConnected);
        //Register Audio focus when service is bound.
        if (isPlayServiceConnected) {
            focusPlayer(); //Focus player page
        }
    }

    @Override
    public void onAudioPlayServiceConnected() {
        Logs.i(TAG, "onAudioPlayServiceConnected()");
        super.onAudioPlayServiceConnected();
        //Focus player page
        focusPlayer();
        //Load fragment by category index
        switchFilterTab(vItems[CATEGORY_TITLE], true);
    }

    @Override
    public void focusPlayer() {
        Logs.i(TAG, "focusPlayer()");
        super.focusPlayer(); //Focus player page
        //Update playing rhythm
        updatePlayRhythmStatus(isPlaying());
    }

    /**
     * Filter item click event.
     */
    private View.OnClickListener mViewOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mIvRhythmAnim) {
                final ProAudio media = getCurrMedia();
                if (media != null) {
                    Log.i(TAG, "Audio rhythm : Open playing media...");
                    openPlayerActivity();
                }

                //Click collect /folder /name /artist /album
            } else {
                switchFilterTab(v, true);
            }
        }
    };

    /**
     * Switch filter item.
     *
     * @param v          Filter item that focused.
     * @param isExecLoad true : Load fragment focused.
     */
    private void switchFilterTab(View v, boolean isExecLoad) {
        final int loop = vItems.length;
        for (int idx = 0; idx < loop; idx++) {
            View item = vItems[idx];
            if (item == v) {
                item.setFocusable(true);
                item.requestFocus();
                setTabBg(item, true);
                if (isExecLoad) {
                    loadFragment(idx, null);
                }
            } else {
                item.setFocusable(false);
                item.clearFocus();
                setTabBg(item, false);
            }
        }
    }

    /**
     * Set background of filter items.
     *
     * @param vFocused Filter item that focused.
     * @param selected Focused or not.
     */
    private void setTabBg(View vFocused, boolean selected) {
        if (selected) {
            SkinUtil.instance().setViewBackground(this, vFocused, R.drawable.bg_title_item_c);
        } else {
            SkinUtil.instance().setViewBackground(this, vFocused, R.drawable.btn_filter_tab_selector);
        }
    }

    /**
     * Load fragment content.
     *
     * @param idx The idx of fragment
     */
    private void loadFragment(int idx, String[] params) {
        Log.i(TAG, "loadFragment(" + idx + ")");
        BaseAudioFragment fragToLoad = null;
        switch (idx) {
            case CATEGORY_COLLECT:
                if (!(mFragCurrent instanceof AudioCollectFragment)) {
                    fragToLoad = new AudioCollectFragment();
                }
                break;
            case CATEGORY_FOLDER:
                if (mFragCurrent instanceof AudioFolderFragment) {
                    int pageLayer = mFragCurrent.getPageLayer();
                    if (pageLayer == 1) {
                        fragToLoad = new AudioFolderFragment();
                    }
                } else {
                    fragToLoad = new AudioFolderFragment();
                }
                break;
            case CATEGORY_TITLE:
                if (!(mFragCurrent instanceof AudioTitleFragment)) {
                    fragToLoad = new AudioTitleFragment();
                }
                break;
            case CATEGORY_ARTIST:
                if (mFragCurrent instanceof AudioArtistFragment) {
                    int pageLayer = mFragCurrent.getPageLayer();
                    if (pageLayer == 1) {
                        fragToLoad = new AudioArtistFragment();
                    }
                } else {
                    fragToLoad = new AudioArtistFragment();
                }
                break;
            case CATEGORY_ALBUM:
                if (mFragCurrent instanceof AudioAlbumFragment) {
                    int pageLayer = mFragCurrent.getPageLayer();
                    if (pageLayer == 1) {
                        fragToLoad = new AudioAlbumFragment();
                    }
                } else {
                    fragToLoad = new AudioAlbumFragment();
                }
                break;
        }

        //
        if (fragToLoad != null) {
            // Remove old
            if (mFragCurrent != null) {
                FragUtil.removeV4Fragment(mFragCurrent, getSupportFragmentManager());
                mFragCurrent = null;
            }
            // Load new
            mFragCurrent = fragToLoad;
            mFragCurrent.setParams(params);
            FragUtil.loadV4Fragment(R.id.layout_frag, mFragCurrent, getSupportFragmentManager());
        }
    }

    /**
     * Open player
     *
     * @param mediaUrl The media url to play.
     * @param position The position to play.
     */
    public void playAndOpenPlayerActivity(String mediaUrl, int position) {
        Log.i(TAG, "playAndOpenPlayerActivity(" + mediaUrl + "," + position + ")");
        // Apply play information.
        applyPlayInfo(mediaUrl, position);
        // Check if already playing.
        if (isPlayingSameMedia(mediaUrl)) {
            Log.i(TAG, "### The media to play is already playing now. ###");
        } else {
            Logs.i("TIME_COL", "-3-" + System.currentTimeMillis());
            playByUrlByUser(mediaUrl);
        }
        //Open player.
        openPlayerActivity();
    }

    /**
     * Open player activity
     */
    private void openPlayerActivity() {
        Log.i(TAG, "openPlayerActivity()");
        if (getCurrMedia() != null) {
            Log.i(TAG, "openPlayerActivity() -EXEC-");
            Intent playerIntent = new Intent(mContext, MusicPlayerActivity.class);
            startActivityForResult(playerIntent, 1);
        }
    }

    @Override
    public void onMountStateChanged(List listStorageDevices) {
        super.onMountStateChanged(listStorageDevices);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onMountStateChanged()");
                Map<String, StorageDevice> mapStorage = SDCardUtils.getMapMountedUsb(mContext);
                if (mapStorage == null || mapStorage.size() == 0) {
                    Log.i(TAG, "onMountStateChanged() -exitApplication()-");
                    exitApplication();
                }
            }
        });
    }

    @Override
    public void onScanStateChanged(final int state) {
        super.onScanStateChanged(state);
        Logs.i(TAG, "onScanStateChanged(" + MediaScanState.desc(state) + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFragCurrent != null) {
                    mFragCurrent.onScanStateChanged(state);
                }
            }
        });
    }

    //TODO (1)目前每10条数据刷新一次，太频繁，当达到一定的数量，如100条记录，可以放缓加载速度，如每100条刷新一次；当加载完成后，可以通过SCAN状态来执行一次总的刷新
    //TODO (2)在加载过程中，不应每次跳转到播放位置，否则会造成播放列表不稳定。
    @Override
    public void onGotDeltaMedias(final List listMedias) {
        super.onGotDeltaMedias(listMedias);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logs.i(TAG, "onGotDeltaMedias(" + (listMedias == null ? null : listMedias.size()) + ")");
                if (mFragCurrent != null) {
                    mFragCurrent.onGotDeltaMedias(listMedias);
                }
            }
        });
    }

    /**
     * Demand : Automatically open player after 5s.
     *
     * @param isAutoOpen <p>true: Automatically open player after 5s;</p>
     *                   <p>false:
     *                   (1)Cancel on touch.
     *                   (2)Cancel on receive key event from seek+/seek-
     *                   (3)Cancel on activity execute {@link #moveTaskToBack(boolean)}
     *                   </p>
     */
    private void autoOpenPlayer(boolean isAutoOpen) {
        if (isAutoOpen) {
            Log.i(TAG, "autoOpenPlayer(true)");
            mHandler.sendEmptyMsgDelayed(this, MyHandler.MSG_AUTO_OPEN_PLAYER, MyHandler.DELAY_TIME_OPEN_PLAYER);
        } else {
            if (mHandler.hasMessages(MyHandler.MSG_AUTO_OPEN_PLAYER)) {
                Log.i(TAG, "autoOpenPlayer(false)");
                mHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    private static class MyHandler extends Handler {
        // Auto open player.
        static final int MSG_AUTO_OPEN_PLAYER = 1;
        static final int DELAY_TIME_OPEN_PLAYER = 5000;
        private WeakReference<MusicListActivity> mWeakReference;

        void sendEmptyMsgDelayed(MusicListActivity activity, int what, long delayMillis) {
            if (mWeakReference == null || mWeakReference.get() == null) {
                mWeakReference = new WeakReference<>(activity);
            }
            sendEmptyMessageDelayed(what, delayMillis);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mWeakReference != null && mWeakReference.get() != null) {
                MusicListActivity activity = mWeakReference.get();
                switch (msg.what) {
                    case MSG_AUTO_OPEN_PLAYER:
                        activity.openPlayerActivity();
                        break;
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        autoOpenPlayer(false);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onGotKey(int keyCode) {
        Logs.i(TAG, "onGotKey(" + keyCode + ")");
        moveFocusToRubbish(mVRubbishFocus);
        switch (keyCode) {
            case KeyCodes.KEYCODE_MEDIA_PREVIOUS:
            case KeyCodes.KEYCODE_MEDIA_NEXT:
            case KeyCodes.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyCodes.KEYCODE_MEDIA_PLAY:
            case KeyCodes.KEYCODE_MEDIA_PAUSE:
                break;
            case KeyCodes.KEYCODE_BACK:
                // onBackPressed();
                break;
            case KeyCodes.KEYCODE_ENTER:
                autoOpenPlayer(false);
                if (mFragCurrent != null) {
                    mFragCurrent.playSelected();
                }
                break;
            case KeyCodes.KEYCODE_DPAD_LEFT:
                autoOpenPlayer(false);
                if (mFragCurrent != null) {
                    mFragCurrent.selectPrev();
                }
                break;
            case KeyCodes.KEYCODE_DPAD_RIGHT:
                autoOpenPlayer(false);
                if (mFragCurrent != null) {
                    mFragCurrent.selectNext();
                }
                break;
        }
    }

    /**
     * Move window focus to rubbish position where not useful.
     *
     * @param vRubbish Rubbish view.
     */
    private void moveFocusToRubbish(View vRubbish) {
        View focusedV = getCurrentFocus();
        if (focusedV != vRubbish && vRubbish != null) {
            vRubbish.setFocusable(true);
            vRubbish.requestFocus();
        }
    }

    @Override
    public void onPlayStateChanged(final int playState) {
        super.onPlayStateChanged(playState);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onPlayStateChanged(" + PlayState.desc(playState) + ")");
                switch (playState) {
                    case PlayState.PLAY:
                    case PlayState.PREPARED:
                        // 播放器聚焦 && 播放许可
                        boolean isPlayEnable = isPlayerFocused() && EgarApiMusic.isPlayEnable(mContext);
                        Logs.i(TAG, "onPlayStateChanged() >> {isPlayEnable:" + isPlayEnable + "}");
                        updatePlayRhythmStatus(isPlayEnable);
                        if (mFragCurrent != null) {
                            mFragCurrent.refreshPlaying(getCurrMedia());
                        }
                        break;
                    case PlayState.REFRESH_UI:
                        break;
                    case PlayState.SEEK_COMPLETED:
                        break;
                    default:
                        updatePlayRhythmStatus(false);
                        break;
                }
            }
        });
    }

    /**
     * Update rhythm status in the right top position.
     */
    private void updatePlayRhythmStatus(boolean isActiveAnim) {
        try {
            Log.i(TAG, "updatePlayRhythmStatus(" + isActiveAnim + ")");
            Drawable drawable = mIvRhythmAnim.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animDrawable = (AnimationDrawable) drawable;
                if (isActiveAnim) {
                    animDrawable.start();
                } else {
                    animDrawable.stop();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "e : " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        String flag = data.getStringExtra("flag");
        Log.i(TAG, "onActivityResult - flag:'" + flag + "'");
        if ("PLAYER_FINISH_ON_DPAD_LEFT".equals(flag)
                || "PLAYER_FINISH_ON_DPAD_RIGHT".equals(flag)) {
            autoOpenPlayer(true);

            //
        } else if ("PLAYER_FINISH_ON_CLICK_TITLE".equals(flag)) {
            switchFilterTab(vItems[2], false);
            loadFragment(2, data.getStringArrayExtra("values"));
        } else if ("PLAYER_FINISH_ON_CLICK_ARTIST".equals(flag)) {
            switchFilterTab(vItems[3], false);
            loadFragment(3, data.getStringArrayExtra("values"));
        } else if ("PLAYER_FINISH_ON_CLICK_ALBUM".equals(flag)) {
            switchFilterTab(vItems[4], false);
            loadFragment(4, data.getStringArrayExtra("values"));
        }
    }

    @Override
    public void onEbCollect(int position, ProAudio media) {
        Logs.i(TAG, "onEbCollect(" + position + "," + media + ")");
        if (mFragCurrent != null) {
            mFragCurrent.onEbCollect(position, media);
        }
    }

    @Override
    public void onBackPressed() {
        //        super.onBackPressed();
        Logs.i(TAG, "onBackPressed()");
        if (mFragCurrent != null) {
            int fragPageLayer = mFragCurrent.getPageLayer();
            Log.i(TAG, "fragPageLayer : " + fragPageLayer);
            switch (fragPageLayer) {
                case 1:
                    if (mFragCurrent instanceof AudioFolderFragment) {
                        loadFragment(CATEGORY_FOLDER, null);
                    } else if (mFragCurrent instanceof AudioArtistFragment) {
                        loadFragment(CATEGORY_ARTIST, null);
                    } else if (mFragCurrent instanceof AudioAlbumFragment) {
                        loadFragment(CATEGORY_ALBUM, null);
                    }
                    break;
                case 0:
                    autoOpenPlayer(false);
                    moveTaskToBack(true);
                    //super.onBackPressed();
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        Logs.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        //        super.overridePendingTransition(enterAnim, exitAnim);
        super.overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        Logs.i(TAG, "finish()");
        super.finish();
        clearActivity();
    }

    @Override
    protected void onDestroy() {
        Logs.i(TAG, "onDestroy()");
        super.onDestroy();
        clearActivity();
    }

    private void clearActivity() {
        Logs.i(TAG, "clearActivity()");
        //Remove from stack.
        removeFromStack(this);
        //Remove event bus.
        removeEbCallback(this);
        //Cancel all task or runnable.
        autoOpenPlayer(false);
        //Unbind play service.
        bindPlayService(false);
    }
}
