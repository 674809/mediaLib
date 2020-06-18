package com.egar.audio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.egar.audio.activity.BaseTestActivity;
import com.egar.audio.adapter.LvAdapter;
import com.egar.audio.utils.AudioUtils;
import com.egar.music.api.EgarApiMusic;
import com.egar.scanner.api.engine.Configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.java.utils.date.DateFormatUtil;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.ProAudioSheet;
import juns.lib.media.bean.ProAudioSheetMapInfo;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.flags.FilterType;
import juns.lib.media.flags.MediaScanState;
import juns.lib.media.flags.PlayMode;
import juns.lib.media.flags.PlayModeSupportType;
import juns.lib.media.flags.PlayState;
import juns.lib.media.utils.SDCardUtils;

/**
 * Test audio play main activity
 *
 * @author Jun.Wang
 */
public class MainActivity extends BaseTestActivity {
    //TAG
    private static final String TAG = "AudioPlayMain";

    //Left
    private Button mBtnGetStorageDevices;
    private Button mBtnGetAllAudios;
    private Button mBtnGetAudiosCount;
    private Button mBtnGetAllMediaSheets, mBtnAddMediaSheet, mBtnUpdateMediaSheet;
    private Button mBtnGetAllMediaSheetMapInfos, mBtnAddMediaSheetMapInfos, mBtnDelMediaSheetMapInfos;

    //Right
    private TextView mTvScanState, mTvMethodTitle;
    private ListView mLvObjects;
    private LvAdapter mLvAdapter;

    //
    private TextView tvInfo, tvTime;
    private ImageView ivPlayPrev, ivPlay, ivPlayNext, ivMusicCover, ivPlayMode;

    //
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Log.i(TAG, "init()");

        //
        mContext = this;

        //
        mBtnGetStorageDevices = (Button) findViewById(R.id.btn_get_all_storage_devices);
        mBtnGetStorageDevices.setOnClickListener(mViewOnClick);

        //
        mBtnGetAllAudios = (Button) findViewById(R.id.btn_get_all_audios);
        mBtnGetAllAudios.setOnClickListener(mViewOnClick);

        //
        mBtnGetAudiosCount = (Button) findViewById(R.id.btn_get_all_audios_count);
        mBtnGetAudiosCount.setOnClickListener(mViewOnClick);

        //
        mBtnAddMediaSheet = (Button) findViewById(R.id.btn_add_media_sheet);
        mBtnAddMediaSheet.setOnClickListener(mViewOnClick);

        mBtnGetAllMediaSheets = (Button) findViewById(R.id.btn_get_media_sheets);
        mBtnGetAllMediaSheets.setOnClickListener(mViewOnClick);

        mBtnUpdateMediaSheet = (Button) findViewById(R.id.btn_update_media_sheet);
        mBtnUpdateMediaSheet.setOnClickListener(mViewOnClick);

        //
        mBtnGetAllMediaSheetMapInfos = (Button) findViewById(R.id.btn_get_media_sheet_map_infos);
        mBtnGetAllMediaSheetMapInfos.setOnClickListener(mViewOnClick);

        mBtnAddMediaSheetMapInfos = (Button) findViewById(R.id.btn_add_media_sheet_map_info);
        mBtnAddMediaSheetMapInfos.setOnClickListener(mViewOnClick);

        mBtnDelMediaSheetMapInfos = (Button) findViewById(R.id.btn_del_media_sheet_map_info);
        mBtnDelMediaSheetMapInfos.setOnClickListener(mViewOnClick);

        //
        mTvScanState = (TextView) findViewById(R.id.tv_scanning_state);
        mTvMethodTitle = (TextView) findViewById(R.id.tv_title_method);

        //
        tvInfo = (TextView) findViewById(R.id.tv_info);
        tvTime = (TextView) findViewById(R.id.tv_time);
        ivMusicCover = (ImageView) findViewById(R.id.iv_cover);

        ivPlayPrev = (ImageView) findViewById(R.id.iv_prev);
        ivPlayPrev.setOnClickListener(mViewOnClick);

        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivPlay.setOnClickListener(mViewOnClick);

        ivPlayNext = (ImageView) findViewById(R.id.iv_next);
        ivPlayNext.setOnClickListener(mViewOnClick);

        ivPlayMode = (ImageView) findViewById(R.id.iv_mode);
        ivPlayMode.setOnClickListener(mViewOnClick);

        //
        mLvObjects = (ListView) findViewById(R.id.lv_objects);
        mLvObjects.setAdapter((mLvAdapter = new LvAdapter(this, 0)));
        mLvObjects.setOnItemClickListener(new LvItemOnClick());

        //If u want use storage , u must first request permission.
        //Only application installed need this step.
        mTvScanState.post(new Runnable() {
            @Override
            public void run() {
                moveFocusToRubbish(mTvScanState);
                if (!Logs.isDebug()) {
                    Logs.switchEnable(false);
                }
                requestPermission();
            }
        });

        //
        bindPlayService(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlayServiceConnected()) {
            focusPlayer();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onAudioPlayServiceConnected() {
        super.onAudioPlayServiceConnected();
        if (isScanning()) {
            mTvScanState.setText("ScanState - SCANNING");
        }
        //Initialize play mode.
        focusPlayer();
        onPlayModeChanged(getPlayMode());
        autoPlay();
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

    private class LvItemOnClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object objItem = parent.getItemAtPosition(position);
            if (objItem instanceof ProAudio) {
                ProAudio media = (ProAudio) objItem;
                applyPlayInfo(media.getMediaUrl(), position);
                playByUrlByUser(media.getMediaUrl());
            }
        }
    }

    private View.OnClickListener mViewOnClick = new View.OnClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View v) {
            if (v == ivPlayPrev) {
                playPrevByUser();
            } else if (v == ivPlay) {
                playOrPauseByUser();
            } else if (v == ivPlayNext) {
                playNextByUser();

            } else if (v == ivPlayMode) {
                switchPlayMode(PlayModeSupportType.NO_ORDER);

            } else if (v == mBtnGetStorageDevices) {
                mTvMethodTitle.setText("getStorageDevices()");
                List list = getStorageDevices();
                mLvAdapter.refreshData(list);

                //
            } else if (v == mBtnGetAllAudios) {
                mTvMethodTitle.setText("getAllMedias()");
                mLvAdapter.refreshData(getAllMedias(FilterType.MEDIA_NAME, null));
                applyPlayList(null);
                //
            } else if (v == mBtnGetAudiosCount) {
                mTvMethodTitle.setText("getCountInDB()");
                String item = "Medias Count - Audio : " + getCountInDB();
                List<String> list = new ArrayList<>();
                list.add(item);
                mLvAdapter.refreshData(list);

                //
            } else if (v == mBtnAddMediaSheet) {
                mTvMethodTitle.setText("addMediaSheet()");
                newMediaSheet();
            } else if (v == mBtnGetAllMediaSheets) {
                mTvMethodTitle.setText("getAllMediaSheets()");
                listAllMediaSheets();
            } else if (v == mBtnUpdateMediaSheet) {
                mTvMethodTitle.setText("updateMediaSheet()");
                updateMediaSheetTime();

                //
            } else if (v == mBtnGetAllMediaSheetMapInfos) {
                mTvMethodTitle.setText("getAllMediaSheetMapInfos()");
                listAllMediaSheetMapInfos();
            } else if (v == mBtnAddMediaSheetMapInfos) {
                mTvMethodTitle.setText("addMediaSheetMapInfos()");
                addMediaSheetMapInfos_();
            } else if (v == mBtnDelMediaSheetMapInfos) {
                mTvMethodTitle.setText("deleteMediaSheetMapInfos()");
                deleteMediaSheetMapInfos_();
            }
        }

        @SuppressWarnings("unchecked")
        private void newMediaSheet() {
            // Create
            ProAudioSheet pas = new ProAudioSheet();
            pas.setTitle("测试歌单");
            pas.setTitlePinYin("CESHIGEDAN");

            // Add
            List list = new ArrayList();
            list.add(pas);
            addMediaSheet(list);

            // List
            listAllMediaSheets();
        }

        private void listAllMediaSheets() {
            List list = getAllMediaSheets(-1);
            mLvAdapter.refreshData(list);

            //Media sheet map information
            mBtnAddMediaSheetMapInfos.setEnabled(true);
            mBtnGetAllMediaSheetMapInfos.setEnabled(true);
            mBtnDelMediaSheetMapInfos.setEnabled(true);
        }

        @SuppressWarnings("unchecked")
        private void updateMediaSheetTime() {
            List list = getAllMediaSheets(-1);
            if (list != null) {
                for (Object objMediaSheet : list) {
                    if (objMediaSheet instanceof ProAudioSheet) {
                        // Create
                        ProAudioSheet mediaSheet = (ProAudioSheet) objMediaSheet;
                        mediaSheet.setUpdateTime(System.currentTimeMillis());
                        // Add
                        List listMediaSheets = new ArrayList();
                        listMediaSheets.add(mediaSheet);
                        int rowId = updateMediaSheet(listMediaSheets);
                        if (rowId > 0) {
                            Log.i("", "");
                        }
                    }
                }
                listAllMediaSheets();
            }
        }

        private void listAllMediaSheetMapInfos() {
            List list = getAllMediaSheetMapInfos(1);
            mLvAdapter.refreshData(list);
        }

        private void addMediaSheetMapInfos_() {
            List list = getAllMediaSheets(-1);
            if (!EmptyUtil.isEmpty(list)) {
                Object objMediaSheet = list.get(0);
                ProAudioSheet pas = (ProAudioSheet) objMediaSheet;

                //Map information.
                List<ProAudioSheetMapInfo> listMediaSheetMapInfos = new ArrayList<>();
                List listMedias = getAllMedias(FilterType.MEDIA_NAME, null);
                if (!EmptyUtil.isEmpty(listMedias) && listMedias.size() <= 20) {
                    for (int LOOP = listMedias.size(), idx = 0; idx < LOOP; idx++) {
                        ProAudio media = (ProAudio) listMedias.get(idx);
                        ProAudioSheetMapInfo pasmi = new ProAudioSheetMapInfo();
                        pasmi.setSheetId(pas.getId());
                        pasmi.setMediaUrl(media.getMediaUrl());
                        listMediaSheetMapInfos.add(pasmi);
                    }
                }
                addMediaSheetMapInfos(listMediaSheetMapInfos);
                listAllMediaSheetMapInfos();
            }
        }

        private void deleteMediaSheetMapInfos_() {
            List list = getAllMediaSheets(-1);
            if (!EmptyUtil.isEmpty(list)) {
                Object objMediaSheet = list.get(0);
                ProAudioSheet pas = (ProAudioSheet) objMediaSheet;
                deleteMediaSheetMapInfos(pas.getId());
                listAllMediaSheetMapInfos();
            }
        }
    };

    @Override
    public void onMountStateChanged(List listStorageDevices) {
        super.onMountStateChanged(listStorageDevices);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onMountStateChanged()");
                Map<String, StorageDevice> mapStorage = SDCardUtils.getMapMountedUsb(mContext);
                if (mapStorage == null || mapStorage.size() == 0) {
                    Log.i(TAG, "onMountStateChanged() -finish()-");
                    finish();
                }
            }
        });
    }

    @Override
    public void onScanStateChanged(final int state) {
        //        super.onScanStateChanged(state);
        Logs.i(TAG, "onScanStateChanged(" + MediaScanState.desc(state) + ")");
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                switch (state) {
                    case MediaScanState.START:
                        mTvScanState.setText("ScanState - START");
                        break;
                    case MediaScanState.SCAN_AUDIO_END:
                    case MediaScanState.END:
                        mTvScanState.setText("ScanState - END");
                        break;
                    default:
                        mTvScanState.setText("ScanState - SCANNING");
                        break;
                }
            }
        });
    }

    @Override
    public void onPlayStateChanged(final int playStateValue) {
        super.onPlayStateChanged(playStateValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                            Toast.makeText(mContext, "《" + mediaWithError.getTitle() + "》播放异常！", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            private void refreshCurrMediaInfo() {
                Log.i(TAG, "refreshCurrMediaInfo()");
                final ProAudio media = getCurrMedia();
                if (media != null) {
                    setMediaInformation(media);
                    mLvAdapter.refreshPlaying(media.getMediaUrl());
                }
            }

            private void setMediaInformation(ProAudio media) {
                // Media Cover
                AudioUtils.setMediaCover(ivMusicCover, media);
                // Title
                StringBuilder sbInfo = new StringBuilder();
                String strTitle = AudioUtils.getMediaTitle(getApplicationContext(), -1, media, true);
                if (ProAudio.UNKNOWN.equals(strTitle)) {
                    sbInfo.append(getString(R.string.unknown_title));
                } else {
                    sbInfo.append(strTitle);
                }
                //Artist
                String strArtist = media.getArtist();
                if (ProAudio.UNKNOWN.equals(strArtist) || EmptyUtil.isEmpty(strArtist)) {
                    sbInfo.append("\n").append(getString(R.string.unknown_artist));
                } else {
                    sbInfo.append("\n").append(strArtist);
                }
                //Album
                String strAlbum = media.getAlbum();
                if (ProAudio.UNKNOWN.equals(strAlbum) || EmptyUtil.isEmpty(strAlbum)) {
                    sbInfo.append("\n").append(getString(R.string.unknown_album));
                } else {
                    sbInfo.append("\n").append(strAlbum);
                }
                tvInfo.setText(sbInfo.toString());
            }

            private void refreshUIOfPlayBtn(int flag) {
                switch (flag) {
                    case 1:
                        ivPlay.setImageResource(R.drawable.ios_op_pause_c);
                        break;
                    case 2:
                        ivPlay.setImageResource(R.drawable.ios_op_play_c);
                        break;
                }
            }
        });
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
        Logs.debugI(TAG, "refreshSeekBar[1] - audio is playing now.");
        String sbInfo = DateFormatUtil.getFormatHHmmss(paramProgress)
                + " / " + DateFormatUtil.getFormatHHmmss(paramDuration);
        tvTime.setText(sbInfo);
    }

    @Override
    public void onPlayProgressChanged(final String mediaPath, final int progress, final int duration) {
        super.onPlayProgressChanged(mediaPath, progress, duration);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshFrameInfo(1, progress, duration);
            }
        });
    }

    @Override
    public void onPlayModeChanged(int newPlayModeValue) {
        //        super.onPlayModeChanged(newPlayModeValue);
        Logs.i(TAG, "onPlayModeChanged(" + PlayMode.desc(newPlayModeValue) + ")");
        switch (newPlayModeValue) {
            case PlayMode.SINGLE:
                ivPlayMode.setImageResource(R.drawable.ios_op_mode_oneloop_c);
                break;
            case PlayMode.RANDOM:
                ivPlayMode.setImageResource(R.drawable.ios_op_mode_random_c);
                break;
            case PlayMode.ORDER:
                break;
            case PlayMode.LOOP:
            default:
                ivPlayMode.setImageResource(R.drawable.ios_op_mode_loop_c);
                break;
        }
    }

    @Override
    public void onGotKey(int keyCode) {
        moveFocusToRubbish(mTvScanState);
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
    public void finish() {
        clearActivity();
        super.finish();
    }

    @Override
    protected void onDestroy() {
//        clearActivity();
        super.onDestroy();
    }

    private void clearActivity() {
        //
        release();
        bindPlayService(false);
    }
}
