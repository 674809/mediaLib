package com.egar.music.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.egar.music.R;
import com.egar.music.activity.MusicListActivity;
import com.egar.music.adapter.AudioFolderListAdapter;
import com.egar.music.adapter.BaseArrAdapter;
import com.egar.music.bean.FilterParams;
import com.js.sidebar.LetterBg;
import com.js.sidebar.LetterSideBar;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.android.view.FrameAnimationController;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.FilterFolder;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.db.tables.AudioTables;
import juns.lib.media.flags.MediaCollectState;
import juns.lib.media.flags.MediaScanState;

public class AudioFolderFragment extends BaseAudioFragment {
    //TAG
    private static final String TAG = "AudioFolderFrag";

    //==========Widgets in this Fragment==========
    protected View contentV;
    protected ListView lvData;
    protected ImageView ivLoading;

    //Letter sidebar
    private LetterSideBar letterSidebar;
    private LetterBg letterCircle;

    //==========Variables in this Fragment==========
    //Attached activity of this fragment.
    protected MusicListActivity mAttachedActivity;

    // Async data loading task.
    // Task for loading medias.
    private FilterLoadingTask mFilterLoadingTask;
    private List<FilterFolder> mListFilters;
    // Task for loading medias.
    private DataLoadingTask mDataLoadingTask;
    private List<ProAudio> mListData;

    /**
     * ListView adapter.
     */
    private AudioFolderListAdapter mDataAdapter;
    /**
     * ListView item click event.
     */
    private LvItemClick mLvItemClick;

    /**
     * ImageView frame animation control
     */
    private FrameAnimationController mFrameAnimController;

    //Handler , used to process common logic which is need running in another time.
    private static Handler mHandler = new Handler();

    @Override
    public String getFlag() {
        return FragFlags.FLAG_FOLDER;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAttachedActivity = (MusicListActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentV = inflater.inflate(R.layout.activity_music_list_frag_folders, container, false);
        return contentV;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDataAdapter.refreshData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        Logs.i(TAG, "-- init() --");
        //----Widgets----
        //Side bar
        letterCircle = (LetterBg) contentV.findViewById(R.id.letter_circle);
        letterCircle.setVisibility(View.INVISIBLE);
        letterSidebar = (LetterSideBar) contentV.findViewById(R.id.lsb);
        letterSidebar.refreshLetters(null);
        letterSidebar.addCallback(new LetterSideBarCallback());
        letterSidebar.setVisibility(View.VISIBLE);

        // Loading
        ivLoading = (ImageView) contentV.findViewById(R.id.iv_loading);
        mFrameAnimController = new FrameAnimationController();
        mFrameAnimController.setIv(ivLoading);
        mFrameAnimController.setFrameImgResArr(LOADING_RES_ID_ARR);
        showLoading(mAttachedActivity.isScanning());

        // ListView
        mDataAdapter = new AudioFolderListAdapter(mAttachedActivity, 0);
        mDataAdapter.setCollectListener(new CollectBtnCallback());

        lvData = (ListView) contentV.findViewById(R.id.lv_datas);
        lvData.setAdapter(mDataAdapter);
        lvData.setOnItemClickListener((mLvItemClick = new LvItemClick()));
        lvData.setOnScrollListener(new LvOnScroll());

        //Loading page
        loadFilters();
    }

    /**
     * Method used to load filters.
     */
    private void loadFilters() {
        if (isAdded()) {
            Logs.i(TAG, "-- loadFilters() --");
            if (mFilterLoadingTask != null) {
                mFilterLoadingTask.cancel(true);
                mFilterLoadingTask = null;
            }
            mDataAdapter.resetSelect();
            mFilterLoadingTask = new FilterLoadingTask(this);
            mFilterLoadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void showLoading(boolean isShow) {
        if (isAdded()) {
            Logs.i(TAG, "-- showLoading(" + isShow + ") --");
            if (isShow) {
                letterSidebar.setVisibility(View.INVISIBLE);
                ivLoading.setVisibility(View.VISIBLE);
                mFrameAnimController.start();
            } else {
                letterSidebar.setVisibility(View.VISIBLE);
                ivLoading.setVisibility(View.INVISIBLE);
                mFrameAnimController.stop();
            }
        }
    }

    @Override
    public int getPageLayer() {
        return EmptyUtil.isEmpty(mListData) ? 0 : 1;
    }

    @Override
    public void refreshPlaying(ProAudio playingMedia) {
        if (!isAdded()) {
            return;
        }

        //
        Logs.i(TAG, "-- refreshPlaying(" + playingMedia.getMediaUrl() + ") --");
        if (mDataAdapter != null) {
            mDataAdapter.refreshPlaying(playingMedia);
        }
    }

    @Override
    public void scrollToPlayingPos(final boolean isWaitLoading) {
        if (!isAdded()) {
            return;
        }

        //
        Logs.i(TAG, "-- scrollToPlayingPos(" + isWaitLoading + ") --");
        mHandler.removeCallbacksAndMessages(null);
        if (isWaitLoading) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToPlayingPos(false);
                }
            }, 300);
        } else {
            try {
                //Filters is showing.
                if (EmptyUtil.isEmpty(mListData)) {
                    if (!EmptyUtil.isEmpty(mListFilters)) {
                        File playingFile = new File(mAttachedActivity.getCurrMediaPath());
                        String playingFolderPath = playingFile.getParentFile().getPath();
                        // Scroll to first position of current page.
                        int posAtPlayFolders = getPosAtPlayFolders(mListFilters, playingFolderPath);
                        if (posAtPlayFolders < 0) {
                            posAtPlayFolders = 0;
                        }
                        int firstPosOfPage = getPageFirstPos(posAtPlayFolders);
                        lvData.setSelection(firstPosOfPage);
                        // Refresh high light letter of left sidebar.
                        FilterFolder firstFilterMediaOfCurrPage = mListFilters.get(firstPosOfPage);
                        char c = firstFilterMediaOfCurrPage.sortStrPinYin.charAt(0);
                        refreshHLLetterOfSideBar(c);
                    }

                    // Second level page is showing.
                } else {
                    // Scroll to first position of current page.
                    int currPos = getPosAtPlayList(mListData, mAttachedActivity.getCurrMediaPath());
                    if (currPos < 0) {
                        currPos = 0;
                    }
                    int firstPosOfPage = getPageFirstPos(currPos);
                    lvData.setSelection(firstPosOfPage);
                    //Refresh high light letter of left sidebar.
                    ProAudio firstMediaOfCurrPage = mListData.get(firstPosOfPage);
                    char c = firstMediaOfCurrPage.getTitlePinYin().charAt(0);
                    refreshHLLetterOfSideBar(c);
                }
            } catch (Exception e) {
                Log.i(TAG, "scrollToPlayingPos() >> e:" + e.getMessage());
            }
        }
    }

    @Override
    public void selectPrev() {
        if (isAdded()) {
            Logs.i(TAG, "-- selectPrev() --");
            try {
                int prevPos;
                int currPos = mDataAdapter.getSelectPos();
                if (currPos == -1) {
                    if (getPageLayer() == 1) {
                        prevPos = getPosAtPlayList(mListData, mAttachedActivity.getCurrMediaPath());
                    } else {
                        prevPos = getPosAtPlayFolders(mListFilters, mAttachedActivity.getCurrMediaPath());
                    }
                } else {
                    prevPos = mDataAdapter.getPrevPos();
                }
                Log.i(TAG, "prevPos~" + prevPos);
                if (getPageLayer() == 1) {
                    if (prevPos < 0 || prevPos >= mListData.size()) {
                        prevPos = mListData.size() - 1;
                    }
                } else {
                    if (prevPos < 0 || prevPos >= mListFilters.size()) {
                        prevPos = mListData.size() - 1;
                    }
                }

                int pageFirstPos = getPageFirstPos(prevPos);
                mDataAdapter.select(prevPos);
                lvData.setSelection(pageFirstPos);
            } catch (Exception e) {
                Logs.i(TAG, "selectPrev() >> e: " + e.getMessage());
            }
        }
    }

    @Override
    public void selectNext() {
        if (isAdded()) {
            Logs.i(TAG, "-- selectNext() --");
            try {
                int nextPos;
                int currPos = mDataAdapter.getSelectPos();
                if (currPos == -1) {
                    if (getPageLayer() == 1) {
                        nextPos = getPosAtPlayList(mListData, mAttachedActivity.getCurrMediaPath());
                    } else {
                        nextPos = getPosAtPlayFolders(mListFilters, mAttachedActivity.getCurrMediaPath());
                    }
                } else {
                    nextPos = mDataAdapter.getNextPos();
                }
                Log.i(TAG, "nextPos~" + nextPos);
                if (getPageLayer() == 1) {
                    if (nextPos < 0 || nextPos >= mListData.size()) {
                        nextPos = 0;
                    }
                } else {
                    if (nextPos < 0 || nextPos >= mListFilters.size()) {
                        nextPos = 0;
                    }
                }

                int pageFirstPos = getPageFirstPos(nextPos);
                mDataAdapter.select(nextPos);
                lvData.setSelection(pageFirstPos);
            } catch (Exception e) {
                Logs.i(TAG, "selectNext() >> e: " + e.getMessage());
            }
        }
    }

    @Override
    public void playSelected() {
        if (isAdded()) {
            int selectPos = mDataAdapter.getSelectPos();
            Log.i(TAG, "playSelected() > selectPos:" + selectPos);
            mLvItemClick.execItemClick(selectPos);
        }
    }

    @Override
    public void onScanStateChanged(int state) {
        Log.i(TAG, "-- onScanStateChanged(" + state + ") --");
        switch (state) {
            case MediaScanState.START:
                showLoading(true);
                break;
            case MediaScanState.SCANNING_END:
                break;
            case MediaScanState.SCAN_AUDIO_END:
            case MediaScanState.END:
                showLoading(false);
                break;
        }
    }

    @Override
    public void onGotDeltaMedias(List listMedias) {
        int pageLayer = getPageLayer();
        if (pageLayer == 0) {
            loadFilters();
        }
    }

    @Override
    public void onDestroy() {
        if (mLvItemClick != null) {
            mLvItemClick.destroy();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mFilterLoadingTask != null) {
            mFilterLoadingTask.cancel(true);
            mFilterLoadingTask = null;
        }
        if (mDataLoadingTask != null) {
            mDataLoadingTask.cancel(true);
            mDataLoadingTask = null;
        }
        if (mFrameAnimController != null) {
            mFrameAnimController.destroy();
            mFrameAnimController = null;
        }
        super.onDestroy();
    }

    /**
     * ListView scroll event.
     */
    private class LvOnScroll implements AbsListView.OnScrollListener {

        private boolean mmIsTouchScrolling;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case SCROLL_STATE_TOUCH_SCROLL:
                    mmIsTouchScrolling = true;
                    Log.i(TAG, "LvOnScroll -SCROLL_STATE_TOUCH_SCROLL-");
                    break;
                case SCROLL_STATE_IDLE:
                    mmIsTouchScrolling = false;
                    Log.i(TAG, "LvOnScroll -SCROLL_STATE_IDLE-");
                    break;
                case SCROLL_STATE_FLING:
                    Log.i(TAG, "LvOnScroll -SCROLL_STATE_FLING-");
                    break;

            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            try {
                if (mmIsTouchScrolling) {
                    int section = mDataAdapter.getSectionForPosition(firstVisibleItem);
                    refreshHLLetterOfSideBar((char) section);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ListView Item Click Event
     */
    private class LvItemClick implements AdapterView.OnItemClickListener {

        private Handler mmHandler = new Handler();

        /**
         * Is ListView Item is Clicking
         */
        boolean mmIsLvItemClicking = false;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Logs.i(TAG, "LvItemClick -> onItemClick(AdapterView," + position + ",id)");
            execItemClick(position);
        }

        private void execItemClick(int position) {
            Object objItem = mDataAdapter.getItem(position);
            if (objItem == null) {
                return;
            }

            //Click filter group
            if (objItem instanceof FilterFolder) {
                // Refresh list
                FilterFolder filterFolder = (FilterFolder) objItem;
                loadMedias(filterFolder.mediaFolder.getPath());

                //Click Media
            } else if (objItem instanceof ProAudio) {
                if (mmIsLvItemClicking) {
                    mmIsLvItemClicking = false;
                    Logs.i(TAG, "##### ---Forbidden click because of frequency !!!--- #####");
                    return;
                } else {
                    mmIsLvItemClicking = true;
                    mmHandler.removeCallbacksAndMessages(null);
                    mmHandler.postDelayed(mmDelayResetClickingFlagRunnable, 1000);
                }

                //
                ProAudio program = (ProAudio) objItem;
                mAttachedActivity.playAndOpenPlayerActivity(program.getMediaUrl(), position);
            }
        }

        private Runnable mmDelayResetClickingFlagRunnable = new Runnable() {

            @Override
            public void run() {
                mmIsLvItemClicking = false;
            }
        };

        private void destroy() {
            mmHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Method used to loading medias.
     *
     * @param mediaFolderPath Selected folder.
     */
    private void loadMedias(String mediaFolderPath) {
        Log.i(TAG, "-- loadMedias(" + mediaFolderPath + ") --");
        if (mDataLoadingTask != null) {
            mDataLoadingTask.cancel(true);
            mDataLoadingTask = null;
        }
        mDataAdapter.resetSelect();
        mDataLoadingTask = new DataLoadingTask(this, mediaFolderPath);
        mDataLoadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 查询一级目录 TASK
     */
    private static class FilterLoadingTask extends AsyncTask<Void, Void, List<FilterFolder>> {
        WeakReference<AudioFolderFragment> mmWeakReferenceContext;

        FilterLoadingTask(AudioFolderFragment frag) {
            frag.mListData = null;
            mmWeakReferenceContext = new WeakReference<>(frag);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<FilterFolder> doInBackground(Void... voids) {
            Log.i(TAG, "DataLoadingTask - doInBackground()");
            List<FilterFolder> list = null;
            try {
                //
                AudioFolderFragment frag = mmWeakReferenceContext.get();
                if (frag != null) {
                    if (frag.mListFilters == null) {
                        list = frag.mAttachedActivity.getFilterFolders();
                    } else {
                        list = frag.mListFilters;
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "");
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<FilterFolder> filterMedias) {
            super.onPostExecute(filterMedias);
            Log.i(TAG, "DataLoadingTask - onPostExecute()");
            AudioFolderFragment frag = mmWeakReferenceContext.get();
            if (frag != null) {
                if (!EmptyUtil.isEmpty(filterMedias)) {
                    frag.showLoading(false);
                }
                frag.mListFilters = filterMedias;
                frag.refreshFilters();
                frag.scrollToPlayingPos(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshFilters() {
        if (!EmptyUtil.isEmpty(mListFilters)) {
            mDataAdapter.refreshData(mListFilters, mAttachedActivity.getCurrMedia());
        }
    }

    /**
     * 查询二级目录 TASK - 某文件夹下面所有媒体文件信息
     */
    private static class DataLoadingTask extends AsyncTask<Void, Void, List<ProAudio>> {
        WeakReference<AudioFolderFragment> mmReference;
        String mmMediaFolderPath;

        DataLoadingTask(AudioFolderFragment frag, String mediaFolderPath) {
            mmReference = new WeakReference<>(frag);
            mmMediaFolderPath = mediaFolderPath;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<ProAudio> doInBackground(Void... voids) {
            Log.i(TAG, "DataLoadingTask - doInBackground()");
            List<ProAudio> list = null;
            try {
                // 查询条件的[列 映射 值];
                Map<String, String> whereColumns = new HashMap<>();
                whereColumns.put(AudioTables.AudioInfoTable.MEDIA_FOLDER_PATH, mmMediaFolderPath);

                // Filter parameters
                FilterParams fps = new FilterParams();
                fps.setFolderPath(mmMediaFolderPath);

                // Query and sync play list.
                list = mmReference.get().mAttachedActivity.getAndSyncMediasByColumns(whereColumns, null, fps.getParams());
            } catch (Exception e) {
                Log.i(TAG, "");
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<ProAudio> audios) {
            super.onPostExecute(audios);
            Log.i(TAG, "DataLoadingTask - onPostExecute()");
            AudioFolderFragment frag = mmReference.get();
            if (frag != null) {
                frag.showLoading(false);
                frag.mListData = audios;
                frag.refreshData();
                frag.scrollToPlayingPos(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshData() {
        if (!EmptyUtil.isEmpty(mListData)) {
            mDataAdapter.refreshData(mListData, mAttachedActivity.getCurrMedia());
        }
    }

    /**
     * Letter side bar touch callback.
     */
    private class LetterSideBarCallback implements LetterSideBar.LetterSideBarListener {

        private Character mmTouchedLetter;

        @Override
        public void callback(int pos, String letter) {
            try {
                Logs.i(TAG, "LetterSideBarCallback -> callback(" + pos + "," + letter + ")");
                mmTouchedLetter = letter.charAt(0);
                int sectionPos = mDataAdapter.getPositionForSection(mmTouchedLetter);
                if (sectionPos != -1) {
                    lvData.setSelection(sectionPos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTouchDown() {
            try {
                Log.i(TAG, "LetterSideBarCallback - onTouchDown()");
                letterCircle.refreshLetter(mmTouchedLetter);
                letterCircle.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTouchMove() {
            try {
                Log.i(TAG, "LetterSideBarCallback - onTouchMove()");
                letterCircle.refreshLetter(mmTouchedLetter);
                letterCircle.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTouchUp() {
            try {
                Log.i(TAG, "LetterSideBarCallback - onTouchUp()");
                letterCircle.setVisibility(View.INVISIBLE);
                refreshHLLetterOfSideBar(mmTouchedLetter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Refresh highlighted fonts.
     *
     * @param c Char of selected.
     */
    private void refreshHLLetterOfSideBar(Character c) {
        if (isAdded() && letterSidebar != null) {
            if (c == null) {
                try {
                    switch (getPageLayer()) {
                        case 1://[二级目录]
                            int currPosAtList = getPosAtPlayList(mListData, mAttachedActivity.getCurrMediaPath());
                            int firstPosOfCurrPage = getPageFirstPos(currPosAtList);
                            ProAudio firstMediaOfCurrPage = mListData.get(firstPosOfCurrPage);
                            c = firstMediaOfCurrPage.getTitlePinYin().charAt(0);
                            break;
                        case 0://[文件夹/表演者/专辑]
                            if (mListFilters != null) {
                                int currPosAtPlayFolders = getPosAtPlayFolders(mListFilters, mAttachedActivity.getCurrMediaPath());
                                int firstPosOfPage = getPageFirstPos(currPosAtPlayFolders);
                                FilterFolder firstFilterMediaOfCurrPage = mListFilters.get(firstPosOfPage);
                                c = firstFilterMediaOfCurrPage.sortStrPinYin.charAt(0);
                            }
                            break;
                    }
                } catch (Exception e) {
                    Log.i(TAG, "refreshHLLetterOfSideBar - e:" + e.getMessage());
                }
            }
            Log.i(TAG, "c:" + c);
            letterSidebar.refreshHlLetter(c);
        }
    }

    /**
     * Collect button call back.
     */
    private class CollectBtnCallback implements BaseArrAdapter.CollectListener {
        @Override
        public void onClickCollectBtn(ImageView ivCollect, int pos) {
            Log.i(TAG, "CollectBtnCallback - onClickCollectBtn(" + ivCollect + "," + pos + ")");
            Object item = mDataAdapter.getItem(pos);
            if (item instanceof ProAudio) {
                ProAudio media = (ProAudio) item;
                switch (media.getCollected()) {
                    case MediaCollectState.UN_COLLECTED:
                        media.setCollected(MediaCollectState.COLLECTED);
                        media.setUpdateTime(System.currentTimeMillis());
                        mAttachedActivity.updateMediaCollect(pos, media);
                        ivCollect.setImageResource(R.drawable.favor_c);
                        //Clear history collect
                        mAttachedActivity.clearHistoryCollect();
                        break;
                    case MediaCollectState.COLLECTED:
                        media.setCollected(MediaCollectState.UN_COLLECTED);
                        media.setUpdateTime(System.currentTimeMillis());
                        mAttachedActivity.updateMediaCollect(pos, media);
                        ivCollect.setImageResource(R.drawable.favor_c_n);
                        break;
                }
            }
        }
    }

    @Override
    public void onEbCollect(int position, ProAudio media) {
        try {
            Logs.i(TAG, "onEbCollect(" + position + "," + media.getMediaUrl() + ")");
            if (mListData != null) {
                ProAudio mediaOfPos = mListData.get(position);
                if (TextUtils.equals(mediaOfPos.getMediaUrl(), media.getMediaUrl())) {
                    mediaOfPos.setCollected(media.getCollected());
                }
                mDataAdapter.refreshData();
            }
        } catch (Exception e) {
            Logs.i(TAG, "onCollected() >> e: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
