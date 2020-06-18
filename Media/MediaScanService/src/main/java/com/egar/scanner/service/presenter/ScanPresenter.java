package com.egar.scanner.service.presenter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.MediaBase;
import juns.lib.media.bean.MediaFile;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.ProImage;
import juns.lib.media.bean.ProVideo;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.db.DBManagerCenter;
import juns.lib.media.flags.MediaScanState;
import juns.lib.media.flags.MediaType;
import juns.lib.media.provider.audio.AudioFilter;
import juns.lib.media.provider.image.ImageFilter;
import juns.lib.media.provider.video.VideoFilter;
import juns.lib.media.utils.SDCardUtils;
import juns.lib.media.utils.ScannerFileUtils;

/**
 * 扫描控制器
 *
 * @author Jun.Wang
 */
public class ScanPresenter extends ScanPresenterBase {
    //TAG
    private static final String TAG = "ScanPresenter";

    //每隔10个体媒体，通知一次增量
    private static final int DELTA_MEDIA_POST_NUM_LIMIT = 10;

    /**
     * {@link Context}
     */
    private Context mContext;

    //已经挂载的所有存储设备集合
    private Map<String, StorageDevice> mMapMountedStorageDevices;

    /**
     * 数据库操作类
     */
    private DBManagerCenter mDBManager;

    /**
     * Thread multiple threads executor.
     */
    private ExecutorService mExecutor;
    private static final int MAX_THREADS = 4;

    /**
     * 扫描 / 解析音频[音频/视频/图片] 线程
     */
    private BaseRunnable mScanRunnable, mParseAudioRunnable, mParseVideoRunnable, mParseImageRunnable;
    //解析媒体线程锁
    private static final Object PARSE_AUDIO_LOCK = new Object();
    private static final Object PARSE_VIDEO_LOCK = new Object();
    private static final Object PARSE_IMAGE_LOCK = new Object();
    //新增媒体集合
    private List<MediaFile> mDeltaAudioFiles = new ArrayList<>();
    private List<MediaFile> mDeltaVideoFiles = new ArrayList<>();
    private List<MediaFile> mDeltaImageFiles = new ArrayList<>();

    public ScanPresenter(Context context) {
        super(context);
        mContext = context;
        mDBManager = new DBManagerCenter(context);
        updateMountedStorages();
    }

    /**
     * 更新挂载信息
     * <p>更新已挂载的[存储设备]信息</p>
     */
    private void updateMountedStorages() {
        mMapMountedStorageDevices = SDCardUtils.getMapMountedStorage(mContext);
        if (mMapMountedStorageDevices != null) {
            mDBManager.bindMounted(new ArrayList<>(mMapMountedStorageDevices.values()));
        }
    }

    @Override
    public void startScan() {
        //        super.startScan();
        //Check thread pool
        //(1) 线程池尚未建立;
        //(2) 线程池执行完毕;
        if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
            Logs.i(TAG, "startScan() -- New thread pool --");
            mExecutor = Executors.newFixedThreadPool(MAX_THREADS);
        }

        //Execute scanning
        Logs.i(TAG, "startScan() -- New scan runnable and run. --");
        clearRunnable(-1);
        mExecutor.execute((mScanRunnable = new ScanRunnable()));
    }

    @Override
    public boolean isScanning(int type) {
        boolean isScanProcessing = isRunnableProcessing(-1);
        if (type == MediaType.AUDIO || type == MediaType.ALL) {
            isScanProcessing = isScanProcessing || isRunnableProcessing(MediaType.AUDIO);
        }
        if (type == MediaType.VIDEO || type == MediaType.ALL) {
            isScanProcessing = isScanProcessing || isRunnableProcessing(MediaType.VIDEO);
        }
        if (type == MediaType.IMAGE || type == MediaType.ALL) {
            isScanProcessing = isScanProcessing || isRunnableProcessing(MediaType.IMAGE);
        }
        return isScanProcessing;
    }

    @Override
    public int updateMediaCollect(int type, List mediasToCollect) {
        Logs.i(TAG, "updateMediaCollect(" + MediaType.desc(type) + "," + mediasToCollect + ")");
        if (mDBManager != null) {
            return mDBManager.updateMediaCollect(type, mediasToCollect);
        }
        return 0;
    }

    @Override
    public int clearHistoryCollect(int mediaType) {
        Logs.i(TAG, "clearHistoryCollect(" + MediaType.desc(mediaType) + ")");
        if (mDBManager != null) {
            return mDBManager.clearHistoryCollect(mediaType);
        }
        return 0;
    }

    @Override
    public long getCountInDB(int type) {
        if (mDBManager != null) {
            return mDBManager.getCountInDB(type);
        }
        return 0;
    }

    @Override
    public List getStorageDevices() {
        List<StorageDevice> respDevices = new ArrayList<>();
        Map<String, StorageDevice> tmpMapDevices = SDCardUtils.getSDCardInfos(mContext);
        for (StorageDevice storage : tmpMapDevices.values()) {
            if (storage.isMounted()) {
                respDevices.add(storage);
            }
        }
        return respDevices;
    }

    @Override
    public void destroy() {
        Logs.i(TAG, "destroy()");
        mRunnableHandler.removeCallbacksAndMessages(null);
        clearRunnable(-1);
        clearRunnable(MediaType.AUDIO);
        clearRunnable(MediaType.VIDEO);
        clearRunnable(MediaType.IMAGE);
        super.destroy();
    }

    /**
     * 查看线程的运行状态
     *
     * @param runnableType -1 {@link ScanRunnable}
     *                     <p>{@link MediaType#AUDIO}</p>
     *                     <p>{@link MediaType#VIDEO}</p>
     *                     <p>{@link MediaType#IMAGE}</p>
     * @return true，表示该类型的线程在运行中。
     */
    private boolean isRunnableProcessing(int runnableType) {
        switch (runnableType) {
            case MediaType.AUDIO:
                return mParseAudioRunnable != null && mParseAudioRunnable.isProcessing();
            case MediaType.VIDEO:
                return mParseVideoRunnable != null && mParseVideoRunnable.isProcessing();
            case MediaType.IMAGE:
                return mParseImageRunnable != null && mParseImageRunnable.isProcessing();
            case -1:
            default:
                return mScanRunnable != null && mScanRunnable.isProcessing();
        }
    }

    /**
     * 清除线程
     *
     * @param runnableType -1 {@link ScanRunnable}
     *                     <p>{@link MediaType#AUDIO}</p>
     *                     <p>{@link MediaType#VIDEO}</p>
     *                     <p>{@link MediaType#IMAGE}</p>
     */
    private void clearRunnable(int runnableType) {
        switch (runnableType) {
            case MediaType.AUDIO:
                if (mParseAudioRunnable != null) {
                    mParseAudioRunnable.setForceBreak(true);
                    mParseAudioRunnable = null;
                }
                break;
            case MediaType.VIDEO:
                if (mParseVideoRunnable != null) {
                    mParseVideoRunnable.setForceBreak(true);
                    mParseVideoRunnable = null;
                }
                break;
            case MediaType.IMAGE:
                if (mParseImageRunnable != null) {
                    mParseImageRunnable.setForceBreak(true);
                    mParseImageRunnable = null;
                }
                break;
            case -1:
            default:
                if (mScanRunnable != null) {
                    mScanRunnable.setForceBreak(true);
                    mScanRunnable = null;
                }
                break;
        }
    }

    @Override
    public void onRespMounted(List listStorageDevices) {
        Logs.i(TAG, "onRespMounted(" + (listStorageDevices == null ? 0 : listStorageDevices.size()) + ")");
        super.onRespMounted(listStorageDevices);
        startScan();
    }

    @Override
    public void onRespUMounted(List listStorageDevices) {
        Logs.i(TAG, "onRespUMounted(" + (listStorageDevices == null ? 0 : listStorageDevices.size()) + ")");
        super.onRespUMounted(listStorageDevices);
        startScan();
    }

    /**
     * Handler , use in runnable.
     */
    private Handler mRunnableHandler = new Handler();
    private final long SCAN_SLEEP_TIME = 500;//本线程睡眠时间，目的是为了等待解析线程启动成功。

    private class ScanRunnable extends BaseRunnable {
        //TAG
        final String TAG_INNER = TAG + "-SR";

        // 本地数据MAP
        private Map<String, ? extends MediaBase> mmMapSavedAudios, mmMapSavedVideos, mmMapSavedImages;

        ScanRunnable() {
            Logs.i(TAG_INNER, this + "\n    ....    ScanRunnable()");
            // Notify all client scanning state - MediaScanState.START
            notifyScanState(MediaType.ALL, MediaScanState.START);

            // Force close exist parsing runnable
            clearRunnable(MediaType.AUDIO);
            clearRunnable(MediaType.VIDEO);
            clearRunnable(MediaType.IMAGE);

            // Start new parsing runnable
            mRunnableHandler.removeCallbacksAndMessages(null);
            mRunnableHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Logs.i(TAG_INNER, ScanRunnable.this + "\n    ....    ScanRunnable() -- create parse runnable --");
                    mExecutor.execute((mParseAudioRunnable = new ParseAudioRunnable()));
                    mExecutor.execute((mParseVideoRunnable = new ParseVideoRunnable()));
                    mExecutor.execute((mParseImageRunnable = new ParseImageRunnable()));
                }
            }, SCAN_SLEEP_TIME);
        }

        @Override
        public void setForceBreak(boolean forceBreak) {
            super.setForceBreak(forceBreak);
            if (forceBreak) {
                mRunnableHandler.removeCallbacksAndMessages(null);
                Logs.i(TAG_INNER, ScanRunnable.this + "\n    ....    setForceBreak(true) --");
            }
        }

        @Override
        public void run() {
            //-- START --
            Logs.i(TAG_INNER, "ScanRunnable() -- run() --");
            setProcessing(true); //设置中断标记为true，表示SCANNING开始

            try {
                //update mounted information.
                updateMountedInfo();
                // Execute scanning logic
                execProcessing();
            } catch (Exception e) {
                Logs.i(TAG_INNER, this + "\n    ....    run() >> e: " + e.getMessage());
                // e.printStackTrace();
            }

            //-- Force break --
            if (!isForceBreak()) {
                try {
                    // 这里之所以要睡眠一段时间，是为了保证在结束之前，确保解析线程启动成功。
                    final long TARGET_SLEEP_TIME = SCAN_SLEEP_TIME * 4;
                    Thread.sleep(TARGET_SLEEP_TIME);
                    Logs.i(TAG_INNER, this + "\n    ....    run() >> sleep(" + TARGET_SLEEP_TIME + ")");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // --END--
            setProcessing(false);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --Scanning loop END!!!--");
            // Notify state
            notifyScanState(MediaType.ALL, MediaScanState.SCANNING_END);

            //Release all locks after scanning.
            releaseLock(MediaType.ALL);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --LOCK RELEASE ALL--");
        }

        /**
         * 更新挂载信息
         * <p>更新已挂载的[存储设备]信息</p>
         * <p>更新已有的[音频]信息</p>
         * <p>更新已有的[视频]信息</p>
         * <p>更新已有的[图片]信息</p>
         */
        private void updateMountedInfo() {
            Logs.i(TAG, "updateMountedInfo()");
            updateMountedStorages();//更新已挂载的[存储设备]信息
            mmMapSavedAudios = mDBManager.getMapMedias(MediaType.AUDIO);//更新已有的[音频]信息
            mmMapSavedVideos = mDBManager.getMapMedias(MediaType.VIDEO);//更新已有的[视频]信息
            mmMapSavedImages = mDBManager.getMapMedias(MediaType.IMAGE);//更新已有的[图片]信息
        }

        void execProcessing() throws Exception {
            Logs.i(TAG, "execProcessing()");
            if (EmptyUtil.isEmpty(mMapMountedStorageDevices)) {
                return;
            }
            //Loop list.
            //更新本地数据，新增、时间戳等
            for (Iterator<Map.Entry<String, StorageDevice>> it = mMapMountedStorageDevices.entrySet().iterator();
                 it.hasNext(); ) {
                //TODO　Force break loop
                if (isForceBreak()) {
                    Logs.i(TAG_INNER, this + "\n    ....    execScanning() >> -- Force break loop!!! --");
                    throw new Exception("execScanning() - Scanning loop is forced broken !!!");
                }

                //Parse from set.
                Map.Entry<String, StorageDevice> entry = it.next();
                StorageDevice storageDevice = entry.getValue();

                //Update database media path-related information
                String storageId = storageDevice.getStorageId();
                String rootPath = storageDevice.getRoot();
                Logs.i("StorageInfo", "{storageId:" + storageId + " , rootPath:" + rootPath + "}");
                int resCodeAudio = mDBManager.updateMediaPathInfo(MediaType.AUDIO, storageId, rootPath);
                int resCodeVideo = mDBManager.updateMediaPathInfo(MediaType.VIDEO, storageId, rootPath);
                int resCodeImage = mDBManager.updateMediaPathInfo(MediaType.IMAGE, storageId, rootPath);
                Logs.i(TAG_INNER, "execScanning() >> --Update database--[resultCode: " + (resCodeAudio + resCodeVideo + resCodeImage) + "]");

                // Loop list.
                File rootFolder = new File(rootPath);
                if (rootFolder.exists()) {
                    listMedias(rootFolder, storageId, rootPath);
                }

                //Remove current.
                it.remove();
            }
            // TODO
        }

        private void listMedias(final File folder, final String storageId, final String rootPath) throws Exception {
            Logs.debugI(TAG_INNER, "listMedias(" + folder.getPath() + ")");
            //Start list
            if (TextUtils.isEmpty(folder.getPath())) {
                Logs.debugI(TAG_INNER, "listMedias() >> ERROR :: folder is NULL");
                return;
            }

            //Loop list files or folders
            File[] fileArr = folder.listFiles();
            if (fileArr == null || fileArr.length == 0) {
                Logs.debugI(TAG_INNER, "listMedias() >> [" + folder.getPath() + "] is empty!!!");
                return;
            }

            for (File childFile : folder.listFiles()) {
                //TODO　Force break loop
                if (isForceBreak()) {
                    Logs.i(TAG_INNER, this + "\n    ....    listMedias() >> -- Force break loop!!! --");
                    throw new Exception("listMedias() - Scanning loop is forced broken !!!");
                }

                //TODO 这个判断非常重要，所在存储设备随时可能被移除
                if (!childFile.exists()) {
                    break;
                }

                //Don`t list hidden file.
                if (childFile.isHidden()) {
                    continue;
                }

                //Directory
                if (childFile.isDirectory()) {
                    listMedias(childFile, storageId, rootPath);

                    //File
                } else {
                    String filePath = childFile.getPath();
                    if (AudioFilter.isSupport(filePath)) {
                        Logs.i(TAG_INNER, "listMedias() >> AUDIO-filePath:" + filePath);
                        // 将已存在的记录时间戳更新到最新
                        if (mmMapSavedAudios != null && mmMapSavedAudios.containsKey(filePath)) {
                            int affectedRowsNum = mDBManager.updateMediaTimeStamp(MediaType.AUDIO, filePath);
                            Logs.debugI(TAG_INNER, "listMedias() >> AUDIO-affectedRowsNum : " + affectedRowsNum);
                            // 添加新文件到增量列表
                        } else {
                            mDeltaAudioFiles.add(new MediaFile(storageId, rootPath, childFile));
                            releaseLock(MediaType.AUDIO);
                            Logs.debugI(TAG_INNER, "listMedias() >> --LOCK RELEASE--AUDIO--");
                        }
                    } else if (VideoFilter.isSupport(filePath)) {
                        Logs.i(TAG_INNER, "listMedias() >> VIDEO-filePath:" + filePath);
                        // 将已存在的记录时间戳更新到最新
                        if (mmMapSavedVideos != null && mmMapSavedVideos.containsKey(filePath)) {
                            int affectedRowsNum = mDBManager.updateMediaTimeStamp(MediaType.VIDEO, filePath);
                            Logs.debugI(TAG_INNER, "listMedias() >> VIDEO-affectedRowsNum : " + affectedRowsNum);
                            // 添加新文件到增量列表
                        } else {
                            mDeltaVideoFiles.add(new MediaFile(storageId, rootPath, childFile));
                            releaseLock(MediaType.VIDEO);
                            Logs.debugI(TAG_INNER, "listMedias() >> --LOCK RELEASE--VIDEO--");
                        }
                    } else if (ImageFilter.isSupport(filePath)) {
                        Logs.i(TAG_INNER, "listMedias() >> IMAGE-filePath:" + filePath);
                        // 将已存在的记录时间戳更新到最新
                        if (mmMapSavedImages != null && mmMapSavedImages.containsKey(filePath)) {
                            int affectedRowsNum = mDBManager.updateMediaTimeStamp(MediaType.IMAGE, filePath);
                            Logs.debugI(TAG_INNER, "listMedias() >> IMAGE-affectedRowsNum : " + affectedRowsNum);
                            // 添加新文件到增量列表
                        } else {
                            mDeltaImageFiles.add(new MediaFile(storageId, rootPath, childFile));
                            releaseLock(MediaType.IMAGE);
                            Logs.debugI(TAG_INNER, "listMedias() >> --LOCK RELEASE--IMAGE--");
                        }
                    }
                }
            }
        }

        private void releaseLock(int mediaType) {
            //Notify all when scan end.
            if (mediaType == MediaType.AUDIO || mediaType == MediaType.ALL) {
                synchronized (PARSE_AUDIO_LOCK) {
                    try {
                        PARSE_AUDIO_LOCK.notify();
                    } catch (Exception e) {
                        Log.i(TAG, "");
                    }
                }
            }

            if (mediaType == MediaType.VIDEO || mediaType == MediaType.ALL) {
                synchronized (PARSE_VIDEO_LOCK) {
                    try {
                        PARSE_VIDEO_LOCK.notify();
                    } catch (Exception e) {
                        Log.i(TAG, "");
                    }
                }
            }

            if (mediaType == MediaType.IMAGE || mediaType == MediaType.ALL) {
                synchronized (PARSE_IMAGE_LOCK) {
                    try {
                        PARSE_IMAGE_LOCK.notify();
                    } catch (Exception e) {
                        Log.i(TAG, "");
                    }
                }
            }
        }
    }

    private class ParseAudioRunnable extends BaseParseRunnable<ProAudio> {
        private final String TAG_INNER = TAG + "-PAR";
        private boolean mmFirstExec = true;
        private List<ProAudio> mmDeltaMedias;

        ParseAudioRunnable() {
            Logs.i(TAG_INNER, this + "\n    ....    ParseAudioRunnable()");
        }

        @Override
        public void run() {
            // --START--
            setProcessing(true);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --Parsing loop START!!!--");

            try {
                execProcessing();
            } catch (Exception e) {
                Logs.i(TAG_INNER, this + "\n    ....    run() >> e: " + e.getMessage());
                // e.printStackTrace();
            }

            // --START--
            setProcessing(false);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --Parsing loop END!!!--");
            // Notify state
            notifyScanState(MediaType.AUDIO, MediaScanState.SCAN_AUDIO_END);
        }

        @Override
        void execProcessing() throws Exception {
            Logs.i(TAG, "execProcessing()");
            synchronized (PARSE_AUDIO_LOCK) {
                while (true) {
                    //TODO　Force break loop
                    if (isForceBreak()) {
                        Logs.i(TAG_INNER, this + "\n    ....    run() >> -- Force break loop!!! --");
                        throw new Exception("run() - Parsing loop is forced broken !!!");
                    }

                    //首次启动，先执行等待，因为此时 [扫描线程] 有可能尚未开始执行
                    if (mmFirstExec) {
                        mmFirstExec = false;
                        try {
                            Logs.debugI(TAG_INNER, "run() >> --LOCK--1st--mmFirstExec--");
                            PARSE_AUDIO_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //Check null
                    int deltaFileSize = mDeltaAudioFiles.size();
                    boolean isScanProcessing = isRunnableProcessing(-1);
                    Logs.debugI(TAG_INNER, "run() >> --{deltaFileSize:" + deltaFileSize + ", isScanProcessing: " + isScanProcessing + "}--");
                    if (deltaFileSize == 0) {
                        if (isScanProcessing) {
                            try {
                                Logs.debugI(TAG_INNER, "run() >> --LOCK--2nd--No medias--");
                                PARSE_AUDIO_LOCK.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            cacheMedias(true, null);
                            Logs.i(TAG_INNER, "run() >> --LOOP break!!!--");
                            break;
                        }
                    }

                    //Parse first media.
                    Logs.debugI(TAG_INNER, "run() >> --PARSE LOOP EXEC--");
                    parseFirstMedia();
                }
            }
        }

        @Override
        void parseFirstMedia() {
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> --START--");
            if (mDeltaAudioFiles.size() > 0) {
                MediaFile mediaFile = mDeltaAudioFiles.get(0);
                Logs.debugI(TAG_INNER, "parseFirstMedia() >> --[mediaFile: " + mediaFile.getMediaUrl() + "]");
                mDeltaAudioFiles.remove(0);
                parseMedia(mediaFile);
            }
        }

        @Override
        void parseMedia(MediaFile mediaFile) {
            Logs.debugI(TAG_INNER, "parseFirstMedia(" + mediaFile + ")--START--");
            if (mediaFile == null || !mediaFile.exists()) {
                return;
            }

            //Rename file
            mediaFile.setFile(MediaBase.renameFileWithSpecialName(mediaFile.getFile()));
            String mediaUrl = mediaFile.getMediaUrl();
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> [mediaUrl: " + mediaUrl + "]");

            //Parse detail information
            ProAudio tmpMedia = new ProAudio(mediaUrl, mediaFile.getFile());//New empty with no ID3 information.
            ProAudio.parseMedia(mContext, tmpMedia, mediaFile.getFile()); //Parse media ID3 information.
            tmpMedia.setStorageId(mediaFile.getStorageId());
            tmpMedia.setRootPath(mediaFile.getRootPath());//Set storage root path.

            //Parse cover image.
            // 图片路径
            // "/storage/emulated/0/Android/data/com.egar.scanner/files/.cover_img_audio/_storage_emulated_0_music_ALIVE.png"
            // 位置: "/storage/emulated/0/Android/data/com.egar.scanner/files/.cover_img_audio/"
            // 命名: 媒体文件路径，其中的"/"使用"_"替换掉
            String targetImgPath = ScannerFileUtils.getCoverImgPath(MediaType.AUDIO,
                    mediaFile.getMediaStorePath(), tmpMedia.getTitlePinYin());
            tmpMedia.setCoverUrl(ProAudio.getThumbNailPath(mContext, targetImgPath, mediaUrl));
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> [coverImgPath: " + tmpMedia.getCoverUrl() + "]");

            //Update timestamp
            tmpMedia.setCreateTime(System.currentTimeMillis());//记录创建时间戳 == 实时时间
            tmpMedia.setUpdateTime(tmpMedia.getCreateTime());//记录更新时间戳 == 记录创建时间戳

            //Cache to local database
            cacheMedias(false, tmpMedia);
        }

        @Override
        void cacheMedias(boolean isForce, ProAudio media) {
            Logs.debugI(TAG_INNER, "cacheMedias(" + isForce + "," + media + ")--START--");
            if (media != null) {
                if (mmDeltaMedias == null) {
                    mmDeltaMedias = new ArrayList<>();
                }
                mmDeltaMedias.add(media);
            }

            if (!EmptyUtil.isEmpty(mmDeltaMedias)
                    && (isForce || mmDeltaMedias.size() >= DELTA_MEDIA_POST_NUM_LIMIT)) {
                //Store
                int insertCount = mDBManager.insertNewMedias(MediaType.AUDIO, mmDeltaMedias);
                Logs.debugI(TAG_INNER, "parseFirstMedia() >> [insertCount: " + insertCount + " , mmDeltaMedias: " + mmDeltaMedias + "]");
                //Notify
                notifyDeltaMedias(MediaType.AUDIO, mmDeltaMedias);
                //Reset
                mmDeltaMedias = new ArrayList<>();
            }
        }
    }

    private class ParseVideoRunnable extends BaseParseRunnable<ProVideo> {
        private final String TAG_INNER = TAG + "-PVR";
        private boolean mmFirstExec = true;
        private List<ProVideo> mmDeltaMedias;

        ParseVideoRunnable() {
            Logs.i(TAG_INNER, this + "\n    ....    ParseVideoRunnable()");
        }

        @Override
        public void run() {
            // --START--
            setProcessing(true);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --Parsing loop START!!!--");

            try {
                execProcessing();
            } catch (Exception e) {
                Logs.i(TAG_INNER, this + "\n    ....    run() >> e: " + e.getMessage());
                //                e.printStackTrace();
            }

            // --END--
            setProcessing(false);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --Parsing loop END!!!--");
            // Notify state
            notifyScanState(MediaType.VIDEO, MediaScanState.SCAN_VIDEO_END);
        }

        void execProcessing() throws Exception {
            Logs.i(TAG, "execProcessing()");
            synchronized (PARSE_VIDEO_LOCK) {
                while (true) {
                    //TODO　Force break loop
                    if (isForceBreak()) {
                        Logs.i(TAG_INNER, this + "\n    ....    run() >> -- Force break loop!!! --");
                        throw new Exception("run() - Parsing loop is forced broken !!!");
                    }

                    //首次首先WAIT，等待NOTIFY
                    if (mmFirstExec) {
                        mmFirstExec = false;
                        try {
                            Logs.debugI(TAG_INNER, "run() >> --LOCK--1st--mmFirstExec--");
                            PARSE_VIDEO_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //Check null
                    int deltaFileSize = mDeltaVideoFiles.size();
                    boolean isScanProcessing = isRunnableProcessing(-1);
                    Logs.debugI(TAG_INNER, "run() >> --{deltaFileSize:" + deltaFileSize + ", isScanProcessing: " + isScanProcessing + "}--");
                    if (deltaFileSize == 0) {
                        if (isScanProcessing) {
                            try {
                                Logs.debugI(TAG_INNER, "run() >> --LOCK--2nd--No medias--");
                                PARSE_VIDEO_LOCK.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            cacheMedias(true, null);
                            Logs.i("LOCK_LOG", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>--break--");
                            break;
                        }
                    }

                    //Parse first media.
                    Logs.debugI(TAG_INNER, "run() >> --PARSE LOOP EXEC--");
                    parseFirstMedia();
                }
            }
        }

        @Override
        void parseFirstMedia() {
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> --START--");
            if (mDeltaVideoFiles.size() > 0) {
                MediaFile mediaFile = mDeltaVideoFiles.get(0);
                Logs.debugI(TAG_INNER, "parseFirstMedia() >> --[mediaFile: " + mediaFile.getMediaUrl() + "]");
                mDeltaVideoFiles.remove(0);
                parseMedia(mediaFile);
            }
        }

        @Override
        void parseMedia(MediaFile mediaFile) {
            Logs.debugI(TAG_INNER, "parseFirstMedia(" + mediaFile + ")--START--");
            if (mediaFile == null || !mediaFile.exists()) {
                return;
            }

            //Rename file for special code.
            mediaFile.setFile(MediaBase.renameFileWithSpecialName(mediaFile.getFile()));
            String mediaUrl = mediaFile.getMediaUrl();
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> [mediaUrl: " + mediaUrl + "]");

            //Parse detail information
            ProVideo tmpMedia = new ProVideo(mediaUrl, mediaFile.getFile());//New empty.
            ProVideo.parseMediaScaleInfo(mContext, tmpMedia); //Parse media.
            tmpMedia.setStorageId(mediaFile.getStorageId());
            tmpMedia.setRootPath(mediaFile.getRootPath());//Set storage root path.

            //Parse cover image.
            // 图片路径
            // "/storage/0/Android/data/com.egar.scanner/files/.cover_img_audio/_storage_emulated_0_music_ALIVE.png"
            // 位置: "/storage/emulated/0/Android/data/com.egar.scanner/files/.cover_img_audio/"
            // 命名: 媒体文件路径，其中的"/"使用"_"替换掉
            String targetImgPath = ScannerFileUtils
                    .getCoverImgPath(MediaType.VIDEO, mediaFile.getMediaStorePath(), tmpMedia.getTitlePinYin());
            tmpMedia.setCoverUrl(ProVideo.getThumbNailPath(mContext, targetImgPath, mediaUrl));
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> [coverImgPath: " + tmpMedia.getCoverUrl() + "]");

            //Update timestamp
            tmpMedia.setCreateTime(System.currentTimeMillis());//记录创建时间戳 == 实时时间
            tmpMedia.setUpdateTime(tmpMedia.getCreateTime());//记录更新时间戳 == 记录创建时间戳

            //Cache to local database
            cacheMedias(false, tmpMedia);
        }

        @Override
        void cacheMedias(boolean isForce, ProVideo media) {
            Logs.debugI(TAG_INNER, "cacheMedias(" + isForce + "," + media + ")--START--");
            if (media != null) {
                if (mmDeltaMedias == null) {
                    mmDeltaMedias = new ArrayList<>();
                }
                mmDeltaMedias.add(media);
            }

            if (!EmptyUtil.isEmpty(mmDeltaMedias)
                    && (isForce || mmDeltaMedias.size() >= DELTA_MEDIA_POST_NUM_LIMIT)) {
                //Store
                int insertCount = mDBManager.insertNewMedias(MediaType.VIDEO, mmDeltaMedias);
                Logs.debugI(TAG_INNER, "parseFirstMedia() >> [insertCount: " + insertCount + " , mmDeltaMedias: " + mmDeltaMedias + "]");
                //Notify
                notifyDeltaMedias(MediaType.VIDEO, mmDeltaMedias);
                //Reset
                mmDeltaMedias = new ArrayList<>();
            }
        }
    }

    private class ParseImageRunnable extends BaseParseRunnable<ProImage> {
        private final String TAG_INNER = TAG + "-PIR";
        private boolean mmFirstExec = true;
        private List<ProImage> mmDeltaMedias;

        ParseImageRunnable() {
            Logs.i(TAG_INNER, this + "\n    ....    ParseImageRunnable()");
        }

        @Override
        public void run() {
            // --START--
            setProcessing(true);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --Parsing loop START!!!--");

            try {
                execProcessing();
            } catch (Exception e) {
                Logs.i(TAG_INNER, this + "\n    ....    run() >> e: " + e.getMessage());
            }

            // --END--
            setProcessing(false);
            Logs.i(TAG_INNER, this + "\n    ....    run() >> --Parsing loop END!!!--");
            // Notify state
            notifyScanState(MediaType.IMAGE, MediaScanState.SCAN_IMAGE_END);
        }

        @Override
        void execProcessing() throws Exception {
            Logs.i(TAG, "execProcessing()");
            synchronized (PARSE_IMAGE_LOCK) {
                while (true) {
                    //TODO　Force break loop
                    if (isForceBreak()) {
                        Logs.i(TAG_INNER, this + "\n    ....    run() >> -- Force break loop!!! --");
                        throw new Exception("run() - Parsing loop is forced broken !!!");
                    }

                    //首次首先WAIT，等待NOTIFY
                    if (mmFirstExec) {
                        mmFirstExec = false;
                        try {
                            Logs.debugI(TAG_INNER, "run() >> --LOCK--1st--mmFirstExec--");
                            PARSE_IMAGE_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //Check null
                    int deltaFileSize = mDeltaImageFiles.size();
                    boolean isScanProcessing = isRunnableProcessing(-1);
                    Logs.debugI(TAG_INNER, "run() >> --{deltaFileSize:" + deltaFileSize + ", isScanProcessing: " + isScanProcessing + "}--");
                    if (deltaFileSize == 0) {
                        if (isScanProcessing) {
                            try {
                                Logs.debugI(TAG_INNER, "run() >> --LOCK--2nd--No medias--");
                                PARSE_IMAGE_LOCK.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            cacheMedias(true, null);
                            Logs.i("LOCK_LOG", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>--break--");
                            break;
                        }
                    }

                    //Parse first media.
                    Logs.debugI(TAG_INNER, "run() >> --PARSE LOOP EXEC--");
                    parseFirstMedia();
                }
            }
        }

        @Override
        void parseFirstMedia() {
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> --START--");
            if (mDeltaImageFiles.size() > 0) {
                MediaFile mediaFile = mDeltaImageFiles.get(0);
                Logs.debugI(TAG_INNER, "parseFirstMedia() >> --[mediaFile: " + mediaFile.getMediaUrl() + "]");
                mDeltaImageFiles.remove(0);
                parseMedia(mediaFile);
            }
        }

        @Override
        void parseMedia(MediaFile mediaFile) {
            Logs.debugI(TAG_INNER, "parseFirstMedia(" + mediaFile + ")--START--");
            if (mediaFile == null || !mediaFile.exists()) {
                return;
            }

            //Rename file for special code.
            mediaFile.setFile(MediaBase.renameFileWithSpecialName(mediaFile.getFile()));
            String mediaUrl = mediaFile.getMediaUrl();
            Logs.debugI(TAG_INNER, "parseFirstMedia() >> [mediaUrl: " + mediaUrl + "]");

            //Parse detail information
            ProImage tmpMedia = new ProImage(mediaUrl, mediaFile.getFile());//New empty.
            tmpMedia.setStorageId(mediaFile.getStorageId());
            tmpMedia.setRootPath(mediaFile.getRootPath());//Set storage root path.

            //Update timestamp
            tmpMedia.setCreateTime(System.currentTimeMillis());//记录创建时间戳 == 实时时间
            tmpMedia.setUpdateTime(tmpMedia.getCreateTime());//记录更新时间戳 == 记录创建时间戳

            //Cache to local database
            cacheMedias(false, tmpMedia);
        }

        @Override
        void cacheMedias(boolean isForce, ProImage media) {
            Logs.debugI(TAG_INNER, "cacheMedias(" + isForce + "," + media + ")--START--");
            if (media != null) {
                if (mmDeltaMedias == null) {
                    mmDeltaMedias = new ArrayList<>();
                }
                mmDeltaMedias.add(media);
            }

            if (!EmptyUtil.isEmpty(mmDeltaMedias)
                    && (isForce || mmDeltaMedias.size() >= DELTA_MEDIA_POST_NUM_LIMIT)) {
                //Store
                int insertCount = mDBManager.insertNewMedias(MediaType.IMAGE, mmDeltaMedias);
                Logs.debugI(TAG_INNER, "parseFirstMedia() >> [insertCount: " + insertCount + " , mmDeltaMedias: " + mmDeltaMedias + "]");
                //Notify
                notifyDeltaMedias(MediaType.VIDEO, mmDeltaMedias);
                //Reset
                mmDeltaMedias = new ArrayList<>();
            }
        }
    }

    @Override
    void notifyScanState(int mediaType, int scanState) {
        Logs.i(TAG, "notifyScanState(" + MediaType.desc(mediaType) + ", " + MediaScanState.desc(scanState) + ")");
        switch (scanState) {
            case MediaScanState.SCAN_AUDIO_END:
                super.notifyScanState(mediaType, scanState);
                if (!isScanning(MediaType.AUDIO)) { //如果此时扫描已经停止，表示整个扫描&解析AUDIO过程结束了
                    super.notifyScanState(mediaType, MediaScanState.END);
                    Logs.i(TAG, "notifyScanState(" + MediaType.desc(mediaType) + ", " + MediaScanState.desc(MediaScanState.END) + ")");
                }
                break;
            case MediaScanState.SCAN_VIDEO_END:
                super.notifyScanState(mediaType, scanState);
                if (!isScanning(MediaType.VIDEO)) { //如果此时扫描已经停止，表示整个扫描&解析VIDEO过程结束了
                    super.notifyScanState(mediaType, MediaScanState.END);
                    Logs.i(TAG, "notifyScanState(" + MediaType.desc(mediaType) + ", " + MediaScanState.desc(MediaScanState.END) + ")");
                }
                break;
            case MediaScanState.SCAN_IMAGE_END:
                super.notifyScanState(mediaType, scanState);
                if (!isScanning(MediaType.IMAGE)) { //如果此时扫描已经停止，表示整个扫描&解析IMAGE过程结束了
                    super.notifyScanState(mediaType, MediaScanState.END);
                    Logs.i(TAG, "notifyScanState(" + MediaType.desc(mediaType) + ", " + MediaScanState.desc(MediaScanState.END) + ")");
                }
                break;
            default: // 通知扫描开始/扫描结束等其他状态
                super.notifyScanState(mediaType, scanState);
                break;
        }
    }

    @Override
    void notifyDeltaMedias(int type, List listMedias) {
        super.notifyDeltaMedias(type, listMedias);
    }
}