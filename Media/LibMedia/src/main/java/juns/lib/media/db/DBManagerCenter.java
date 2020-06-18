package juns.lib.media.db;

import android.content.Context;

import java.util.List;
import java.util.Map;

import juns.lib.media.bean.MediaBase;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.db.base.BaseDBManager;
import juns.lib.media.db.manager.AudioDBManager;
import juns.lib.media.db.manager.ImageDBManager;
import juns.lib.media.db.manager.VideoDBManager;
import juns.lib.media.flags.MediaType;

/**
 * Database manager.
 *
 * @author Jun.Wang
 */
public class DBManagerCenter {
    //TAG
    //    private static final String TAG = "DBManagerCenter";

    //数据库操作类
    private BaseDBManager mAudioDbManager, mVideoDbManager, mImageDbManager;

    public DBManagerCenter(Context context) {
        //
        mAudioDbManager = AudioDBManager.instance(context);
        mAudioDbManager.setDbPath(null);
        //
        mVideoDbManager = VideoDBManager.instance(context);
        mVideoDbManager.setDbPath(null);
        //
        mImageDbManager = ImageDBManager.instance(context);
        mImageDbManager.setDbPath(null);
    }

    /**
     * 查询对应媒体记录条数
     */
    public void bindMounted(List<StorageDevice> mountedStorages) {
        mImageDbManager.bindMounted(mountedStorages);
        mVideoDbManager.bindMounted(mountedStorages);
        mAudioDbManager.bindMounted(mountedStorages);
    }

    public Map<String, ? extends MediaBase> getMapMedias(final int mediaType) {
        switch (mediaType) {
            case MediaType.AUDIO:
                return mAudioDbManager.getMapMedias();
            case MediaType.VIDEO:
                return mVideoDbManager.getMapMedias();
            case MediaType.IMAGE:
                return mImageDbManager.getMapMedias();
        }
        return null;
    }

    public int insertNewMedias(final int mediaType, final List<? extends MediaBase> listMedias) {
        switch (mediaType) {
            case MediaType.AUDIO:
                return mAudioDbManager.insertNewMedias(listMedias);
            case MediaType.VIDEO:
                return mVideoDbManager.insertNewMedias(listMedias);
            case MediaType.IMAGE:
                return mImageDbManager.insertNewMedias(listMedias);
        }
        return 0;
    }

    public int updateMediaCollect(int mediaType, List mediasToCollect) {
        switch (mediaType) {
            case MediaType.AUDIO:
                return mAudioDbManager.updateMediaCollect(mediaType, mediasToCollect);
            case MediaType.VIDEO:
                return mVideoDbManager.updateMediaCollect(mediaType, mediasToCollect);
            case MediaType.IMAGE:
                return mImageDbManager.updateMediaCollect(mediaType, mediasToCollect);
        }
        return 0;
    }

    public int clearHistoryCollect(int mediaType) {
        switch (mediaType) {
            case MediaType.AUDIO:
                return mAudioDbManager.clearHistoryCollect();
            case MediaType.VIDEO:
                return mVideoDbManager.clearHistoryCollect();
            case MediaType.IMAGE:
                return mImageDbManager.clearHistoryCollect();
        }
        return 0;
    }

    public int updateMediaTimeStamp(final int mediaType, String mediaUrl) {
        switch (mediaType) {
            case MediaType.AUDIO:
                return mAudioDbManager.updateMediaTimeStamp(mediaUrl);
            case MediaType.VIDEO:
                return mVideoDbManager.updateMediaTimeStamp(mediaUrl);
            case MediaType.IMAGE:
                return mImageDbManager.updateMediaTimeStamp(mediaUrl);
        }
        return 0;
    }

    public int updateMediaPathInfo(int mediaType, String storageId, String rootPath) {
        switch (mediaType) {
            case MediaType.IMAGE:
                return mImageDbManager.updateMediaPathInfo(storageId, rootPath);
            case MediaType.VIDEO:
                return mVideoDbManager.updateMediaPathInfo(storageId, rootPath);
            case MediaType.AUDIO:
                return mAudioDbManager.updateMediaPathInfo(storageId, rootPath);
        }
        return 0;
    }

    /**
     * 查询对应媒体记录条数
     */
    public long getCountInDB(int mediaType) {
        long totalCount = 0;
        if (mediaType == MediaType.IMAGE || mediaType == MediaType.ALL) {
            totalCount += mImageDbManager.getCount();
        }
        if (mediaType == MediaType.VIDEO || mediaType == MediaType.ALL) {
            totalCount += mVideoDbManager.getCount();
        }
        if (mediaType == MediaType.AUDIO || mediaType == MediaType.ALL) {
            totalCount += mAudioDbManager.getCount();
        }
        return totalCount;
    }
}
