package juns.lib.media.db.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.MediaBase;
import juns.lib.media.bean.ProVideo;
import juns.lib.media.db.base.BaseDBManager;
import juns.lib.media.db.helper.VideoDBHelper;
import juns.lib.media.db.tables.VideoTables.VideoInfoTable;
import juns.lib.media.flags.MediaType;
import juns.lib.media.provider.video.VideoParser;
import juns.lib.media.utils.ScannerFileUtils;

/**
 * 该类用来处理数据库操作
 *
 * @author Jun.Wang
 */
public class VideoDBManager extends BaseDBManager {
    // TAG
    private static final String TAG = "VideoDBManager";

    /**
     * 上下文
     */
    private Context mAppContext;

    /**
     * SQLiteDatabase Object
     */
    private SQLiteDatabase mDB;
    private String mDbPath;

    /**
     * 单例对象
     */
    private static VideoDBManager mDBManager;

    private VideoDBManager(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static VideoDBManager instance(Context context) {
        if (mDBManager == null) {
            synchronized (VideoDBManager.class) {
                mDBManager = new VideoDBManager(context);
            }
        }
        return mDBManager;
    }

    @Override
    public void setDbPath(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) {
            mDbPath = ScannerFileUtils.getDbFilePath(MediaType.VIDEO);
        } else {
            mDbPath = dbPath;
        }
    }

    /**
     * 打开数据库连接
     */
    private boolean openDB() {
        if (mDB == null || !mDB.isOpen()) {
            try {
                SQLiteOpenHelper helper = new VideoDBHelper(mAppContext, mDbPath);
                mDB = helper.getWritableDatabase();
            } catch (Exception e) {
                Logs.i(TAG, "openDB() :: Exception-" + e.getMessage());
                e.printStackTrace();
                closeDB();
            }
        }
        return mDB != null && mDB.isOpen();
    }

    /**
     * 关闭数据库连接
     */
    private void closeDB() {
        if (mDB != null) {
            try {
                mDB.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mDB = null;
            }
        }
    }

    @Override
    public long getCount() {
        long count = 0;
        if (openDB()) {
            Exception exception = null;
            try {
                String sql = "select count(" + VideoInfoTable.MEDIA_URL + ") as TOTAL_COUNT from " + VideoInfoTable.T_NAME;
                String selection = getExistDeviceArea(false);
                if (selection != null) {
                    sql += " where " + selection;
                }

                Logs.i(TAG, "getCount() >> [sql : " + sql + "]");
                Cursor cursor = mDB.rawQuery(sql, null);
                if (cursor != null && cursor.moveToFirst()) {
                    count += cursor.getLong(cursor.getColumnIndex("TOTAL_COUNT"));
                    cursor.close();
                }
            } catch (Exception e) {
                Logs.i(TAG, "Cursor getCount() - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return count;
    }

    @Override
    public Map<String, ? extends MediaBase> getMapMedias() {
        HashMap<String, ProVideo> mapMedias = new HashMap<>();

        //Get where
        String selection = getExistDeviceArea(false);
        //Query
        Exception exception = null;
        Cursor cursor = null;
        try {
            cursor = queryMedias(null, selection, null, null, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ProVideo media = VideoParser.parseFromCursor(cursor);
                mapMedias.put(media.getMediaUrl(), media);
            }
        } catch (Exception e) {
            Logs.i(TAG, "Cursor getMapMedias - e :: " + e.getMessage());
            exception = e;
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (exception != null) {
                closeDB();
            }
        }
        return mapMedias;
    }

    @Override
    public int insertNewMedias(List<? extends MediaBase> listMedias) {
        int insertCount = 0;
        if (listMedias != null && openDB()) {
            Exception exception = null;
            try {
                mDB.beginTransaction();
                for (MediaBase mediaBase : listMedias) {
                    //Convert base to real.
                    ProVideo media = (ProVideo) mediaBase;
                    //Construct key-value
                    ContentValues cvs = new ContentValues();
                    //                    cvs.put(VideoInfoTable.ID, media.getId());
                    cvs.put(VideoInfoTable.TITLE, media.getTitle());
                    cvs.put(VideoInfoTable.TITLE_PINYIN, media.getTitlePinYin());
                    cvs.put(VideoInfoTable.FILE_NAME, media.getFileName());
                    cvs.put(VideoInfoTable.STORAGE_ID, media.getStorageId());
                    cvs.put(VideoInfoTable.ROOT_PATH, media.getRootPath());
                    cvs.put(VideoInfoTable.MEDIA_URL, media.getMediaUrl());
                    cvs.put(VideoInfoTable.MEDIA_FOLDER_PATH, media.getMediaFolderPath());
                    cvs.put(VideoInfoTable.MEDIA_FOLDER_NAME, media.getMediaFolderName());
                    cvs.put(VideoInfoTable.MEDIA_FOLDER_NAME_PINYIN, media.getMediaFolderNamePinYin());
                    cvs.put(VideoInfoTable.DURATION, media.getDuration());
                    cvs.put(VideoInfoTable.COLLECTED, media.getCollected());
                    cvs.put(VideoInfoTable.COVER_URL, media.getCoverUrl());
                    cvs.put(VideoInfoTable.WIDTH, media.getWidth());
                    cvs.put(VideoInfoTable.HEIGHT, media.getHeight());
                    cvs.put(VideoInfoTable.ROTATION, media.getRotation());
                    cvs.put(VideoInfoTable.CAPTION, media.getCaption());
                    cvs.put(VideoInfoTable.CREATE_TIME, media.getCreateTime());
                    cvs.put(VideoInfoTable.UPDATE_TIME, media.getUpdateTime());
                    long rowId = mDB.insert(VideoInfoTable.T_NAME, null, cvs);
                    if (rowId > 0) {
                        insertCount++;
                    }
                }
                mDB.setTransactionSuccessful();
            } catch (Exception e) {
                Logs.i(TAG, "insertNewMedias - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (mDB != null) {
                    mDB.endTransaction();
                }
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return insertCount;
    }

    @Override
    public int updateMediaTimeStamp(String mediaUrl) {
        int affectedRowNum = 0;
        if (openDB()) {
            Exception exception = null;
            try {
                //values
                ContentValues values = new ContentValues();
                values.put(VideoInfoTable.UPDATE_TIME, System.currentTimeMillis());
                //whereClause
                String whereClause = VideoInfoTable.MEDIA_URL + "=?";
                //whereArgs
                String[] whereArgs = new String[]{mediaUrl};
                affectedRowNum = mDB.update(VideoInfoTable.T_NAME, values, whereClause, whereArgs);
            } catch (Exception e) {
                Logs.i(TAG, "updateMediaTimeStamp - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return affectedRowNum;
    }

    @Override
    public synchronized int updateMediaPathInfo(String storageId, String rootPath) {
        int resultCode = 0;
        Logs.i(TAG, "updateMediaPathInfo(" + storageId + "," + rootPath + ")");
        if (openDB()) {
            Exception exception = null;
            try {
                // 查询存储设备的ID
                storageId = TextUtils.isEmpty(storageId) ? "''" : storageId;
                // -1- 判断根路径是否发生改变
                //查询已经保存的根路径
                String oldRootPath = "";
                final String sqlQueryRootPath = "select " + VideoInfoTable.ROOT_PATH
                        + " from " + VideoInfoTable.T_NAME
                        + " where " + VideoInfoTable.STORAGE_ID + "='" + storageId + "'  limit 1";
                Logs.i(TAG, "updateMediaPathInfo - SqlQueryRootPath: " + sqlQueryRootPath);
                Cursor cursor = mDB.rawQuery(sqlQueryRootPath, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        oldRootPath = cursor.getString(cursor.getColumnIndex(VideoInfoTable.ROOT_PATH));
                    }
                    cursor.close();
                }
                //如果检测到根路径字符串没有发生改变，则中断执行
                if (TextUtils.isEmpty(oldRootPath) || TextUtils.equals(oldRootPath, rootPath)) {
                    Logs.i(TAG, "updateMediaPathInfo - SqlQueryRootPath: break for root path not changed!!!");
                    return resultCode;
                }

                //
                String oldCoverImgNamePrefix = oldRootPath.replace("/", "_");
                String newCoverImgNamePrefix = rootPath.replace("/", "_");
                //
                final String sqlUpdateRootPath = "update " + VideoInfoTable.T_NAME
                        + " set "
                        + VideoInfoTable.ROOT_PATH + "='" + rootPath + "'"
                        + "," + VideoInfoTable.MEDIA_URL
                        + "=replace(" + VideoInfoTable.MEDIA_URL + ",'" + oldRootPath + "','" + rootPath + "')"
                        + "," + VideoInfoTable.COVER_URL
                        + "=replace(" + VideoInfoTable.COVER_URL + ",'" + oldRootPath + "','" + rootPath + "')"
                        + "," + VideoInfoTable.COVER_URL
                        + "=replace(" + VideoInfoTable.COVER_URL + ",'" + oldCoverImgNamePrefix + "','" + newCoverImgNamePrefix + "')"
                        + " where "
                        + VideoInfoTable.STORAGE_ID + "='" + storageId + "'"
                        + " and "
                        + VideoInfoTable.ROOT_PATH + "!='" + rootPath + "'";
                Logs.debugI(TAG, "updateMediaPathInfo - sql : " + sqlUpdateRootPath);
                mDB.execSQL(sqlUpdateRootPath);
                resultCode = 1;
            } catch (Exception e) {
                Logs.i(TAG, "updateMediaPathInfo - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return resultCode;
    }

    @Override
    public int updateMediaCollect(int type, List mediasToCollect) {
        return 0;
    }

    @Override
    public int clearHistoryCollect() {
        return 0;
    }

    /**
     * Provider actions - queryMedias
     */
    @Override
    public Cursor queryMedias(String[] columns,
                              String selection,
                              String[] selectionArgs,
                              String groupBy,
                              String having,
                              String orderBy) {
        Cursor cursor = null;
        if (openDB()) {
            Exception exception = null;
            try {
                //
                String[] SRC_COLUMNS = new String[]{VideoInfoTable.ID, VideoInfoTable.TITLE, VideoInfoTable.TITLE_PINYIN, VideoInfoTable.FILE_NAME
                        , VideoInfoTable.STORAGE_ID, VideoInfoTable.ROOT_PATH
                        , VideoInfoTable.MEDIA_URL, VideoInfoTable.MEDIA_FOLDER_PATH, VideoInfoTable.MEDIA_FOLDER_NAME, VideoInfoTable.MEDIA_FOLDER_NAME_PINYIN
                        , VideoInfoTable.DURATION, VideoInfoTable.COLLECTED, VideoInfoTable.COVER_URL
                        , VideoInfoTable.WIDTH, VideoInfoTable.HEIGHT, VideoInfoTable.ROTATION, VideoInfoTable.CAPTION
                        , VideoInfoTable.CREATE_TIME, VideoInfoTable.UPDATE_TIME};

                //Table
                String table = VideoInfoTable.T_NAME;
                //columns
                columns = EmptyUtil.isEmpty(columns) ? SRC_COLUMNS : columns;
                //selection
                if (selection == null) {
                    selection = getExistDeviceArea(false);
                } else {
                    selection += " and " + getExistDeviceArea(false);
                }
                //selectionArgs
                //groupBy
                //having
                //orderBy
                orderBy = TextUtils.isEmpty(orderBy) ? VideoInfoTable.TITLE_PINYIN : orderBy;

                //
                cursor = mDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            } catch (Exception e) {
                Logs.i(TAG, "Cursor queryMedias - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return cursor;
    }

    /**
     * Provider actions - deleteMedias
     */
    @Override
    public int deleteMedias(@Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * Provider actions - updateMedias
     */
    @Override
    public int updateMedias(@Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * Provider actions - queryMediaSheets
     */
    @Override
    public Cursor queryMediaSheets(@Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    /**
     * Provider actions - insertNewMediaSheets
     */
    @Override
    public long insertNewMediaSheets(ContentValues values) {
        return 0;
    }

    /**
     * Provider actions - updateMediaSheets
     */
    @Override
    public long updateMediaSheets(@Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * Provider actions - queryMediaSheetMapInfos
     */
    @Override
    public Cursor queryMediaSheetMapInfos(@Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    /**
     * Provider actions - insertNewMediaSheetMapInfos
     */
    @Override
    public long insertNewMediaSheetMapInfos(ContentValues values) {
        return 0;
    }

    /**
     * Provider actions - deleteMediaSheetMapInfos
     */
    @Override
    public long deleteMediaSheetMapInfos(String where, String[] whereArgs) {
        return 0;
    }
}
