package com.egar.scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.egar.scanner.activity.BaseFragActivity;
import com.egar.scanner.adapter.LvAdapter;
import com.egar.scanner.api.engine.Configs;

import java.util.ArrayList;
import java.util.List;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.ProAudioSheet;
import juns.lib.media.bean.ProAudioSheetMapInfo;
import juns.lib.media.bean.ProImage;
import juns.lib.media.bean.ProVideo;
import juns.lib.media.flags.FilterType;
import juns.lib.media.flags.MediaScanState;
import juns.lib.media.flags.MediaType;

/**
 * Scanner test main activity
 *
 * @author Jun.Wang
 */
public class MainActivity extends BaseFragActivity {
    //TAG
    private static final String TAG = "ScannerMain";

    //Left
    private Button mBtnStartScan;
    private Button mBtnGetStorageDevices;
    private Button mBtnGetAllMediaSheets, mBtnAddMediaSheet, mBtnUpdateMediaSheet;
    private Button mBtnGetAllMediaSheetMapInfos, mBtnAddMediaSheetMapInfos, mBtnDelMediaSheetMapInfos;
    private Button mBtnGetAllAudios, mBtnGetAllVideos, mBtnGetAllImages, mBtnGetAllMedias;
    private Button mBtnGetAudiosCount, mBtnGetVideosCount, mBtnGetImagesCount, mBtnGetMediasCount;

    //Right
    private TextView tvScanState, tvMethodTitle;

    //
    private ListView mLv;
    private LvAdapter mLvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Logs.debugI(TAG, "init()");

        //
        mBtnStartScan = (Button) findViewById(R.id.btn_start_scan);
        mBtnStartScan.setOnClickListener(mViewOnClick);

        mBtnGetStorageDevices = (Button) findViewById(R.id.btn_get_all_storage_devices);
        mBtnGetStorageDevices.setOnClickListener(mViewOnClick);

        //
        mBtnGetAllMediaSheets = (Button) findViewById(R.id.btn_get_media_sheets);
        mBtnGetAllMediaSheets.setOnClickListener(mViewOnClick);
        mBtnGetAllMediaSheets.setEnabled(false);

        mBtnAddMediaSheet = (Button) findViewById(R.id.btn_add_media_sheet);
        mBtnAddMediaSheet.setOnClickListener(mViewOnClick);
        mBtnAddMediaSheet.setEnabled(false);

        mBtnUpdateMediaSheet = (Button) findViewById(R.id.btn_update_media_sheet);
        mBtnUpdateMediaSheet.setOnClickListener(mViewOnClick);
        mBtnUpdateMediaSheet.setEnabled(false);

        //
        mBtnGetAllMediaSheetMapInfos = (Button) findViewById(R.id.btn_get_media_sheet_map_infos);
        mBtnGetAllMediaSheetMapInfos.setOnClickListener(mViewOnClick);
        mBtnGetAllMediaSheetMapInfos.setEnabled(false);

        mBtnAddMediaSheetMapInfos = (Button) findViewById(R.id.btn_add_media_sheet_map_info);
        mBtnAddMediaSheetMapInfos.setOnClickListener(mViewOnClick);
        mBtnAddMediaSheetMapInfos.setEnabled(false);

        mBtnDelMediaSheetMapInfos = (Button) findViewById(R.id.btn_del_media_sheet_map_info);
        mBtnDelMediaSheetMapInfos.setOnClickListener(mViewOnClick);
        mBtnDelMediaSheetMapInfos.setEnabled(false);

        //
        mBtnGetAllAudios = (Button) findViewById(R.id.btn_get_all_audios);
        mBtnGetAllAudios.setOnClickListener(mViewOnClick);
        mBtnGetAllAudios.setEnabled(false);

        mBtnGetAllVideos = (Button) findViewById(R.id.btn_get_all_videos);
        mBtnGetAllVideos.setOnClickListener(mViewOnClick);
        mBtnGetAllVideos.setEnabled(false);

        mBtnGetAllImages = (Button) findViewById(R.id.btn_get_all_images);
        mBtnGetAllImages.setOnClickListener(mViewOnClick);
        mBtnGetAllImages.setEnabled(false);

        mBtnGetAllMedias = (Button) findViewById(R.id.btn_get_all_medias);
        mBtnGetAllMedias.setOnClickListener(mViewOnClick);
        mBtnGetAllMedias.setEnabled(false);

        //
        mBtnGetAudiosCount = (Button) findViewById(R.id.btn_get_all_audios_count);
        mBtnGetAudiosCount.setOnClickListener(mViewOnClick);
        mBtnGetAudiosCount.setEnabled(false);

        mBtnGetVideosCount = (Button) findViewById(R.id.btn_get_all_videos_count);
        mBtnGetVideosCount.setOnClickListener(mViewOnClick);
        mBtnGetVideosCount.setEnabled(false);

        mBtnGetImagesCount = (Button) findViewById(R.id.btn_get_all_images_count);
        mBtnGetImagesCount.setOnClickListener(mViewOnClick);
        mBtnGetImagesCount.setEnabled(false);

        mBtnGetMediasCount = (Button) findViewById(R.id.btn_get_all_medias_count);
        mBtnGetMediasCount.setOnClickListener(mViewOnClick);
        mBtnGetMediasCount.setEnabled(false);

        //
        tvScanState = (TextView) findViewById(R.id.tv_scanning_state);
        tvMethodTitle = (TextView) findViewById(R.id.tv_title_method);

        //
        mLv = (ListView) findViewById(R.id.lv);
        mLv.setAdapter((mLvAdapter = new LvAdapter(this, 0)));

        //
        bindScanService(MediaType.ALL, true);

        //If u want use storage , u must first request permission.
        //Only application installed need this step.
        tvScanState.post(new Runnable() {
            @Override
            public void run() {
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

    private View.OnClickListener mViewOnClick = new View.OnClickListener() {

        @SuppressWarnings("unchecked")
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View v) {
            if (v == mBtnStartScan) {
                if (!Logs.isDebug()) {
                    Logs.switchEnable(false);
                    requestPermission();
                } else {
                    tvMethodTitle.setText("startScan()");
                    startScan();
                }
            } else if (v == mBtnGetStorageDevices) {
                tvMethodTitle.setText("getStorageDevices()");
                List list = getStorageDevices();
                if (list != null) {
                    Log.i(TAG, "list : " + list.size());
                    mLvAdapter.refreshData(list);
                }

                //
            } else if (v == mBtnGetAllAudios) {
                tvMethodTitle.setText("getAllMedias(MediaType.AUDIO)");
                loopSetMedias(getAllMedias(MediaType.AUDIO, FilterType.MEDIA_NAME, null));
            } else if (v == mBtnGetAllVideos) {
                tvMethodTitle.setText("getAllMedias(MediaType.VIDEO)");
                loopSetMedias(getAllMedias(MediaType.VIDEO, FilterType.MEDIA_NAME, null));
            } else if (v == mBtnGetAllImages) {
                tvMethodTitle.setText("getAllMedias(MediaType.IMAGE)");
                loopSetMedias(getAllMedias(MediaType.IMAGE, FilterType.MEDIA_NAME, null));
            } else if (v == mBtnGetAllMedias) {
                tvMethodTitle.setText("getAllMedias(MediaType.ALL)");
                loopSetMedias(getAllMedias(MediaType.ALL, FilterType.MEDIA_NAME, null));

                //
            } else if (v == mBtnGetAudiosCount) {
                tvMethodTitle.setText("getCount(MediaType.AUDIO)");
                List listStr = new ArrayList();
                listStr.add("Medias Count - Audio : " + getCountInDB(MediaType.AUDIO));
                mLvAdapter.refreshData(listStr);
            } else if (v == mBtnGetVideosCount) {
                tvMethodTitle.setText("getCount(MediaType.VIDEO)");
                List listStr = new ArrayList();
                listStr.add("Medias Count - Video : " + getCountInDB(MediaType.VIDEO));
                mLvAdapter.refreshData(listStr);
            } else if (v == mBtnGetImagesCount) {
                tvMethodTitle.setText("getCount(MediaType.IMAGE)");
                List listStr = new ArrayList();
                listStr.add("Medias Count - Image : " + getCountInDB(MediaType.IMAGE));
                mLvAdapter.refreshData(listStr);
            } else if (v == mBtnGetMediasCount) {
                tvMethodTitle.setText("getCount(MediaType.ALL)");
                List listStr = new ArrayList();
                listStr.add("Medias Count - ALL : " + getCountInDB(MediaType.ALL));
                mLvAdapter.refreshData(listStr);

                //
            } else if (v == mBtnGetAllMediaSheets) {
                tvMethodTitle.setText("getAllMediaSheets()");
                listAllMediaSheets();
            } else if (v == mBtnAddMediaSheet) {
                tvMethodTitle.setText("addMediaSheet()");
                newMediaSheet();
            } else if (v == mBtnUpdateMediaSheet) {
                tvMethodTitle.setText("updateMediaSheet()-Audio");
                updateMediaSheetTime();

            } else if (v == mBtnGetAllMediaSheetMapInfos) {
                tvMethodTitle.setText("getAllMediaSheetMapInfos()");
                listAllMediaSheetMapInfos();
            } else if (v == mBtnAddMediaSheetMapInfos) {
                tvMethodTitle.setText("addMediaSheetMapInfos()");
                addMediaSheetMapInfos_();
            } else if (v == mBtnDelMediaSheetMapInfos) {
                tvMethodTitle.setText("deleteMediaSheetMapInfos()");
                deleteMediaSheetMapInfos_();
            }
        }

        @SuppressWarnings("unchecked")
        private void listAllMediaSheets() {
            List list = getAllMediaSheets(MediaType.AUDIO, -1);
            if (list != null) {
                List listStr = new ArrayList();
                for (Object objMediaSheet : list) {
                    if (objMediaSheet instanceof ProAudioSheet) {
                        ProAudioSheet mediaSheet = (ProAudioSheet) objMediaSheet;
                        listStr.add(mediaSheet.getId()
                                + " - " + mediaSheet.getTitle()
                                + " - UpdateTime: " + mediaSheet.getUpdateTime());
                    }
                }
                mLvAdapter.refreshData(listStr);

                //Media sheet map information
                mBtnAddMediaSheetMapInfos.setEnabled(true);
                mBtnGetAllMediaSheetMapInfos.setEnabled(true);
                mBtnDelMediaSheetMapInfos.setEnabled(true);
            }
        }

        @SuppressWarnings("unchecked")
        private void newMediaSheet() {
            // Create sheet.
            ProAudioSheet pas = new ProAudioSheet();
            pas.setTitle("测试歌单");
            pas.setTitlePinYin("CESHIGEDAN");

            // add media sheet
            List pasToAdd = new ArrayList();
            pasToAdd.add(pas);
            addMediaSheet(MediaType.AUDIO, pasToAdd);

            // List all media sheets
            listAllMediaSheets();
        }

        @SuppressWarnings("unchecked")
        private void updateMediaSheetTime() {
            List list = getAllMediaSheets(MediaType.AUDIO, -1);
            if (list != null) {
                List listStr = new ArrayList();
                for (Object objMediaSheet : list) {
                    if (objMediaSheet instanceof ProAudioSheet) {
                        // Create sheet.
                        ProAudioSheet mediaSheet = (ProAudioSheet) objMediaSheet;
                        mediaSheet.setUpdateTime(System.currentTimeMillis());
                        // Update sheet.
                        List pasToAdd = new ArrayList();
                        pasToAdd.add(mediaSheet);
                        int rowId = updateMediaSheet(MediaType.AUDIO, pasToAdd);
                        if (rowId > 0) {
                            listStr.add(mediaSheet.getId() + " - " + mediaSheet.getTitle() + " - UpdateTime: " + mediaSheet.getUpdateTime());
                        }
                    }
                }
                mLvAdapter.refreshData(listStr);
            }
        }

        @SuppressWarnings("unchecked")
        private void listAllMediaSheetMapInfos() {
            List list = getAllMediaSheetMapInfos(MediaType.AUDIO, 1);
            if (list != null) {
                List listStr = new ArrayList();
                for (int LOOP = list.size(), idx = 0; idx < LOOP; idx++) {
                    ProAudioSheetMapInfo pasmi = (ProAudioSheetMapInfo) list.get(idx);
                    listStr.add(pasmi.getId() + " - " + "storageId:" + pasmi.getSheetId() + " - " + pasmi.getMediaUrl());
                }
                mLvAdapter.refreshData(listStr);
            }
        }

        private void addMediaSheetMapInfos_() {
            List list = getAllMediaSheets(MediaType.AUDIO, -1);
            if (!EmptyUtil.isEmpty(list)) {
                Object objMediaSheet = list.get(0);
                ProAudioSheet pas = (ProAudioSheet) objMediaSheet;

                //Map information.
                List<ProAudioSheetMapInfo> listMediaSheetMapInfos = new ArrayList<>();
                List listMedias = getAllMedias(MediaType.AUDIO, FilterType.MEDIA_NAME, null);
                if (!EmptyUtil.isEmpty(listMedias) && listMedias.size() <= 20) {
                    for (int LOOP = listMedias.size(), idx = 0; idx < LOOP; idx++) {
                        ProAudio media = (ProAudio) listMedias.get(idx);
                        ProAudioSheetMapInfo pasmi = new ProAudioSheetMapInfo();
                        pasmi.setSheetId(pas.getId());
                        pasmi.setMediaUrl(media.getMediaUrl());
                        listMediaSheetMapInfos.add(pasmi);
                    }
                }
                addMediaSheetMapInfos(MediaType.AUDIO, listMediaSheetMapInfos);
                listAllMediaSheetMapInfos();
            }
        }

        private void deleteMediaSheetMapInfos_() {
            List list = getAllMediaSheets(MediaType.AUDIO, -1);
            if (!EmptyUtil.isEmpty(list)) {
                Object objMediaSheet = list.get(0);
                ProAudioSheet pas = (ProAudioSheet) objMediaSheet;
                deleteMediaSheetMapInfos(MediaType.AUDIO, pas.getId());
                listAllMediaSheetMapInfos();
            }
        }

        @SuppressWarnings("unchecked")
        private void loopSetMedias(List list) {
            if (list != null) {
                List listStr = new ArrayList();
                for (int LOOP = list.size(), idx = 0; idx < LOOP; idx++) {
                    Object objMedia = list.get(idx);
                    String mediaType;
                    if (objMedia instanceof ProAudio) {
                        mediaType = "Audio";
                        ProAudio media = (ProAudio) objMedia;
                        listStr.add(mediaType + " - " + media.getId() + " - " + media.getTitle() + "\n" + media.getMediaUrl());
                    } else if (objMedia instanceof ProVideo) {
                        mediaType = "Video";
                        ProVideo media = (ProVideo) objMedia;
                        listStr.add(mediaType + " - " + media.getId() + " - " + media.getTitle() + "\n" + media.getMediaUrl());
                    } else {
                        mediaType = "Image";
                        ProImage media = (ProImage) objMedia;
                        listStr.add(mediaType + " - " + media.getId() + " - " + media.getTitle() + "\n" + media.getMediaUrl());
                    }
                }
                mLvAdapter.refreshData(listStr);
            }
        }
    };

    @Override
    public void onRespScanState(final int state) {
        super.onRespScanState(state);
        final String stateDesc = MediaScanState.desc(state);
        Logs.i(TAG, "onRespScanState(" + stateDesc + ")");
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                tvScanState.setText("ScanState - " + stateDesc);
                checkMedias(getAllMedias(MediaType.ALL, FilterType.NOTHING, null));
            }
        });
    }

    @Override
    public void onRespDeltaMedias(final List listMedias) {
        super.onRespDeltaMedias(listMedias);
        Logs.i(TAG, "onRespScanState(" + ((listMedias == null) ? 0 : listMedias.size()) + ")");
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                tvScanState.setText("ScanState - SCANNING");
                checkMedias(listMedias);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void checkMedias(List listMedias) {
        long mediaCountAudio = getCountInDB(MediaType.AUDIO);
        Logs.debugI(TAG, "mediaCountAudio : " + mediaCountAudio);
        long mediaCountVideo = getCountInDB(MediaType.VIDEO);
        Logs.debugI(TAG, "mediaCountVideo : " + mediaCountVideo);
        long mediaCountImage = getCountInDB(MediaType.IMAGE);
        Logs.debugI(TAG, "mediaCountImage : " + mediaCountImage);

        //
        List<String> listStrs = new ArrayList<>();
        listStrs.add("TOTAL Medias - ALL : " + (mediaCountAudio + mediaCountVideo + mediaCountImage));
        mLvAdapter.refreshData(listStrs);

        //
        if (!EmptyUtil.isEmpty(listMedias)) {
            //
            mBtnGetAllAudios.setEnabled(true);
            mBtnGetAllVideos.setEnabled(true);
            mBtnGetAllImages.setEnabled(true);
            mBtnGetAllMedias.setEnabled(true);
            //
            mBtnGetMediasCount.setEnabled(true);
            mBtnGetAudiosCount.setEnabled(true);
            mBtnGetVideosCount.setEnabled(true);
            mBtnGetImagesCount.setEnabled(true);
            //
            mBtnAddMediaSheet.setEnabled(true);
            mBtnGetAllMediaSheets.setEnabled(true);
            mBtnUpdateMediaSheet.setEnabled(true);
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
        bindScanService(MediaType.ALL, false);
    }
}
