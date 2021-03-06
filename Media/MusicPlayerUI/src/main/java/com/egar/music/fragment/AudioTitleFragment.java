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
import com.egar.music.adapter.AudioNameListAdapter;
import com.egar.music.adapter.BaseArrAdapter;
import com.js.sidebar.LetterBg;
import com.js.sidebar.LetterSideBar;

import java.lang.ref.WeakReference;
import java.util.List;

import juns.lib.android.utils.Logs;
import juns.lib.android.view.FrameAnimationController;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.flags.FilterType;
import juns.lib.media.flags.MediaCollectState;
import juns.lib.media.flags.MediaScanState;

public class AudioTitleFragment extends BaseAudioFragment {
    //TAG
    private static final String TAG = "AudioTitleFrag";

    //==========Widgets in this Fragment==========
    //Base layout
    private View contentV;

    //ListView
    private ListView lvData;

    //Left loading frame animation
    private ImageView ivLoading;

    //Letter sidebar
    private LetterSideBar letterSidebar;
    private LetterBg letterCircle;

    //==========Variables in this Activity==========
    //Activity context
    private MusicListActivity mAttachedActivity;

    // ImageView frame animation control
    private FrameAnimationController mFrameAnimController;

    //Handler , used to process common logic which is need running in another time.
    private static Handler mHandler = new Handler();

    //Task for loading medias.
    private DataLoadingTask mDataLoadingTask;

    //Loading
    private List<ProAudio> mListData;
    private AudioNameListAdapter mDataAdapter;
    /**
     * ListView item click event.
     */
    private LvItemClick mLvItemClick;

    @Override
    public String getFlag() {
        return FragFlags.FLAG_TITLE;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAttachedActivity = (MusicListActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentV = inflater.inflate(R.layout.activity_music_list_frag_names, container, false);
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
        //----Widgets----
        // >> Left side bar <<
        letterCircle = (LetterBg) contentV.findViewById(R.id.letter_circle);
        letterCircle.setVisibility(View.INVISIBLE);

        letterSidebar = (LetterSideBar) contentV.findViewById(R.id.lsb);
        letterSidebar.refreshLetters(null);
        letterSidebar.addCallback(new LetterSideBarCallback());

        ivLoading = (ImageView) contentV.findViewById(R.id.iv_loading);
        mFrameAnimController = new FrameAnimationController();
        mFrameAnimController.setIv(ivLoading);
        mFrameAnimController.setFrameImgResArr(LOADING_RES_ID_ARR);
        showLoading(mAttachedActivity.isScanning());

        // >> ListView <<
        mDataAdapter = new AudioNameListAdapter(mAttachedActivity);
        mDataAdapter.setCollectListener(new CollectBtnCallback());

        lvData = (ListView) contentV.findViewById(R.id.lv_datas);
        lvData.setAdapter(mDataAdapter);
        lvData.setOnItemClickListener((mLvItemClick = new LvItemClick()));
        lvData.setOnScrollListener(new LvOnScroll());

        //
        loadMedias();
    }

    @Override
    protected void showLoading(boolean isShow) {
        if (isAdded()) {
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

    private void loadMedias() {
        if (mDataLoadingTask != null) {
            mDataLoadingTask.cancel(true);
            mDataLoadingTask = null;
        }
        mDataLoadingTask = new DataLoadingTask(this);
        mDataLoadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class DataLoadingTask extends AsyncTask<Void, Void, List<ProAudio>> {
        WeakReference<AudioTitleFragment> mmReference;

        DataLoadingTask(AudioTitleFragment frag) {
            mmReference = new WeakReference<>(frag);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<ProAudio> doInBackground(Void... voids) {
            try {
                Logs.i(TAG, "DataLoadingTask - doInBackground()");
                AudioTitleFragment frag = mmReference.get();
                return frag.mAttachedActivity.getAndSyncAllMedias(FilterType.MEDIA_NAME, null);
            } catch (Exception e) {
                Log.i(TAG, "");
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ProAudio> audios) {
            super.onPostExecute(audios);
            try {
                Logs.i(TAG, "DataLoadingTask - onPostExecute()");
                AudioTitleFragment frag = mmReference.get();
                if (!EmptyUtil.isEmpty(audios)) {
                    frag.showLoading(false);
                }
                frag.mListData = audios;
                frag.refreshData();
                frag.scrollToPlayingPos(true);
                frag.mAttachedActivity.autoPlay();
            } catch (Exception e) {
                Log.i(TAG, "");
            }
        }
    }

    private void refreshData() {
        if (mListData != null) {
            mDataAdapter.refreshData(mListData, mAttachedActivity.getCurrMedia());
        }
    }

    @Override
    public int getPageLayer() {
        return 0;
    }

    @Override
    public void refreshPlaying(ProAudio playingMedia) {
        if (mDataAdapter != null) {
            mDataAdapter.refreshPlaying(playingMedia);
        }
    }

    @Override
    public void scrollToPlayingPos(final boolean isWaitLoading) {
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
                //
                int currPos = mAttachedActivity.getCurrPos();
                int firstPosOfPage = getPageFirstPos(currPos);
                lvData.setSelection(firstPosOfPage);
                //
                ProAudio firstMediaOfCurrPage = mListData.get(firstPosOfPage);
                char c = firstMediaOfCurrPage.getTitlePinYin().charAt(0);
                refreshHLLetterOfSideBar(c);
            } catch (Exception e) {
                Log.i(TAG, "refreshHLLetterOfSideBar() >> e:" + e.getMessage());
            }
        }
    }

    @Override
    public void selectPrev() {
        if (isAdded()) {
            try {
                int prevPos;
                int currPos = mDataAdapter.getSelectPos();
                if (currPos == -1) {
                    prevPos = getPosAtPlayList(mListData, mAttachedActivity.getCurrMediaPath());
                } else {
                    prevPos = mDataAdapter.getPrevPos();
                }
                Log.i(TAG, "prevPos~" + prevPos);
                if (prevPos < 0 || prevPos >= mListData.size()) {
                    prevPos = mListData.size() - 1;
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
            try {
                int nextPos;
                int currPos = mDataAdapter.getSelectPos();
                if (currPos == -1) {
                    nextPos = getPosAtPlayList(mListData, mAttachedActivity.getCurrMediaPath());
                } else {
                    nextPos = mDataAdapter.getNextPos();
                }
                Log.i(TAG, "nextPos~" + nextPos);
                if (nextPos < 0 || nextPos >= mListData.size()) {
                    nextPos = 0;
                }

                int pageFirstPos = getPageFirstPos(nextPos);
                mDataAdapter.select(nextPos);
                lvData.setSelection(pageFirstPos);
            } catch (Exception e) {
                Logs.i(TAG, "selectPrev() >> e: " + e.getMessage());
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
        switch (state) {
            case MediaScanState.START:
                showLoading(true);
                break;
            case MediaScanState.SCANNING_END:
                break;
            case MediaScanState.SCAN_AUDIO_END:
            case MediaScanState.END:
                showLoading(false);
                loadMedias();
                break;
        }
    }

    @Override
    public void onGotDeltaMedias(List listMedias) {
        loadMedias();
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
                    Logs.i(TAG, "LetterSideBarCallback -> callback(" + pos + "," + letter + "-" + sectionPos + ")");
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
            // 如果传入的字符为NULL,设置为“当前显示的第一条音频数据 的 音频名称”
            if (c == null) {
                try {
                    int currPosAtList = getPosAtPlayList(mListData, mAttachedActivity.getCurrMediaPath());
                    int firstPosOfCurrPage = getPageFirstPos(currPosAtList);
                    ProAudio firstMediaOfCurrPage = mListData.get(firstPosOfCurrPage);
                    c = firstMediaOfCurrPage.getTitlePinYin().charAt(0);
                } catch (Exception e) {
                    Log.i(TAG, "refreshHLLetterOfSideBar() >> e:" + e.getMessage());
                }
            }
            Log.i(TAG, "c:" + c);
            letterSidebar.refreshHlLetter(c);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        if (mLvItemClick != null) {
            mLvItemClick.destroy();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
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
     * ListView Item Click Event
     */
    private class LvItemClick implements AdapterView.OnItemClickListener {

        private boolean mmIsLvItemClicking;
        private Handler mmHandler = new Handler();

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Logs.i(TAG, "LvItemClick -> onItemClick(AdapterView," + position + ",id)");
            Logs.i("TIME_COL", "-1-" + System.currentTimeMillis());
            execItemClick(position);
        }

        private Runnable mmDelayResetClickingFlagRunnable = new Runnable() {

            @Override
            public void run() {
                mmIsLvItemClicking = false;
            }
        };

        private void execItemClick(int position) {
            ProAudio itemMedia = mDataAdapter.getItem(position);
            if (itemMedia == null) {
                return;
            }

            if (mmIsLvItemClicking) {
                Log.i(TAG, "##### ---Forbidden click because of frequency !!!--- #####");
                return;
            } else {
                mmIsLvItemClicking = true;
                mmHandler.removeCallbacksAndMessages(null);
                mmHandler.postDelayed(mmDelayResetClickingFlagRunnable, 1000);
            }

            //
            Logs.i("TIME_COL", "-2-" + System.currentTimeMillis());
            mAttachedActivity.playAndOpenPlayerActivity(itemMedia.getMediaUrl(), position);
        }

        void destroy() {
            mmHandler.removeCallbacksAndMessages(null);
        }
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
            if (mmIsTouchScrolling) {
                int section = mDataAdapter.getSectionForPosition(firstVisibleItem);
                Character firstVisibleChar = (char) section;
                Logs.i(TAG, "LvOnScroll > onScroll() > [firstVisibleChar : " + firstVisibleChar + "]");
                if (letterSidebar == null) {
                    refreshHLLetterOfSideBar(firstVisibleChar);
                } else if (letterSidebar.getHlLetter() != firstVisibleChar) {
                    refreshHLLetterOfSideBar(firstVisibleChar);
                }
            }
        }
    }

    /**
     * Collect operate callback.
     */
    private class CollectBtnCallback implements BaseArrAdapter.CollectListener {
        @Override
        public void onClickCollectBtn(ImageView ivCollect, int pos) {
            ProAudio item = mDataAdapter.getItem(pos);
            if (item == null) {
                return;
            }

            switch (item.getCollected()) {
                case MediaCollectState.UN_COLLECTED:
                    item.setCollected(MediaCollectState.COLLECTED);
                    item.setUpdateTime(System.currentTimeMillis());
                    mAttachedActivity.updateMediaCollect(pos, item);
                    ivCollect.setImageResource(R.drawable.favor_c);
                    //Clear history collect
                    mAttachedActivity.clearHistoryCollect();
                    break;
                case MediaCollectState.COLLECTED:
                    item.setCollected(MediaCollectState.UN_COLLECTED);
                    item.setUpdateTime(System.currentTimeMillis());
                    mAttachedActivity.updateMediaCollect(pos, item);
                    ivCollect.setImageResource(R.drawable.favor_c_n);
                    break;
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
