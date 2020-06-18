package juns.lib.media.db.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.MediaBase;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.db.base.BaseDBManager;
import juns.lib.media.db.helper.AudioDBHelper;
import juns.lib.media.db.tables.AudioTables.AudioInfoTable;
import juns.lib.media.db.tables.AudioTables.AudioSheetMapInfoTable;
import juns.lib.media.db.tables.AudioTables.AudioSheetTable;
import juns.lib.media.flags.MediaType;
import juns.lib.media.provider.audio.AudioParser;
import juns.lib.media.utils.ScannerFileUtils;

/**
 * 该类用来处理数据库操作
 *
 * @author Jun.Wang
 */
public class AudioDBManager extends BaseDBManager {
    // TAG
    private static final String TAG = "AudioDBManager";

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
    private static AudioDBManager mDBManager;

    private AudioDBManager(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static AudioDBManager instance(Context context) {
        if (mDBManager == null) {
            synchronized (AudioDBManager.class) {
                mDBManager = new AudioDBManager(context);
            }
        }
        return mDBManager;
    }

    @Override
    public void setDbPath(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) {
            mDbPath = ScannerFileUtils.getDbFilePath(MediaType.AUDIO);
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
                SQLiteOpenHelper helper = new AudioDBHelper(mAppContext, mDbPath);
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
                String sql = "select count(" + AudioInfoTable.MEDIA_URL + ") as TOTAL_COUNT from " + AudioInfoTable.T_NAME;
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
    public Map<String, ? extends ProAudio> getMapMedias() {
        HashMap<String, ProAudio> mapMedias = new HashMap<>();

        //Get where
        String selection = getExistDeviceArea(false);
        //Query
        Exception exception = null;
        Cursor cursor = null;
        try {
            cursor = queryMedias(null, selection, null, null, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ProAudio media = AudioParser.parseFromCursor(cursor);
                if (media != null) {
                    mapMedias.put(media.getMediaUrl(), media);
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "Cursor queryMedias - e :: " + e.getMessage());
            exception = e;
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (exception != null) {
                closeDB();
            }
        }
        return mapMedias;
    }

    public MediaBase getMedia(String mediaUrl) {
        ProAudio media = null;
        //Get where
        String selection = null;
        if (!EmptyUtil.isEmpty(mediaUrl)) {
            selection = AudioInfoTable.MEDIA_URL + "='" + mediaUrl + "'";
        }

        //Query
        Exception exception = null;
        Cursor cursor = null;
        try {
            cursor = queryMedias(null, selection, null, null, null, null);
            if (cursor.moveToFirst()) {
                media = AudioParser.parseFromCursor(cursor);
            }
        } catch (Exception e) {
            Logs.i(TAG, "getMedia(mediaUrl) >> e: " + e.getMessage());
            exception = e;
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (exception != null) {
                closeDB();
            }
        }
        return media;
    }

    /**
     * Get Music List
     */
    public List<ProAudio> getListMusics(String title, String artist) {
        Log.i(TAG, "getListMusics(" + title + "," + artist + ")");
        List<ProAudio> listMusics = new ArrayList<>();
        Throwable throwable = null;
        Cursor cur = null;
        try {
            //
            openDB();

            //
            String table = AudioInfoTable.T_NAME;
            String[] columns = new String[]{"*"};

            String selection = null;
            if (!EmptyUtil.isEmpty(title) && EmptyUtil.isEmpty(artist)) {
                selection = AudioInfoTable.TITLE + " like '%" + title + "%'";
            } else if (!EmptyUtil.isEmpty(title) && !EmptyUtil.isEmpty(artist)) {
                selection = AudioInfoTable.TITLE + " like '%" + title + "%'" +
                        " and " + AudioInfoTable.ARTIST + " like '%" + artist + "%'";
            } else if (EmptyUtil.isEmpty(title) && !EmptyUtil.isEmpty(artist)) {
                selection = AudioInfoTable.ARTIST + " like '%" + artist + "%'";
            }

            //
            Log.i(TAG, "selection : " + selection);
            if (selection != null) {
                cur = mDB.query(table, columns, selection, null, null, null, null, null);
                if (cur != null) {
                    for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                        ProAudio music = AudioParser.parseFromCursor(cur);
                        if (music != null) {
                            listMusics.add(music);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throwable = e;
            Log.i(TAG, "e-" + e.getMessage());
        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
            if (throwable != null) {
                closeDB();
            }
        }
        return listMusics;
    }

    @Override
    public int insertNewMedias(final List<? extends MediaBase> listMedias) {
        int insertCount = 0;
        if (listMedias != null && openDB()) {
            Exception exception = null;
            try {
                mDB.beginTransaction();
                for (MediaBase mediaBase : listMedias) {
                    //Convert base to real.
                    ProAudio media = (ProAudio) mediaBase;
                    //Construct key-value
                    ContentValues cvs = new ContentValues();
                    //                    cvs.put(AudioInfoTable.ID, media.getId());
                    cvs.put(AudioInfoTable.TITLE, media.getTitle());
                    cvs.put(AudioInfoTable.TITLE_PINYIN, media.getTitlePinYin());
                    cvs.put(AudioInfoTable.FILE_NAME, media.getFileName());
                    cvs.put(AudioInfoTable.ALBUM_ID, media.getAlbumID());
                    cvs.put(AudioInfoTable.ALBUM, media.getAlbum());
                    cvs.put(AudioInfoTable.ALBUM_PINYIN, media.getAlbumPinYin());
                    cvs.put(AudioInfoTable.ARTIST, media.getArtist());
                    cvs.put(AudioInfoTable.ARTIST_PINYIN, media.getArtistPinYin());
                    cvs.put(AudioInfoTable.STORAGE_ID, media.getStorageId());
                    cvs.put(AudioInfoTable.ROOT_PATH, media.getRootPath());
                    cvs.put(AudioInfoTable.MEDIA_URL, media.getMediaUrl());
                    cvs.put(AudioInfoTable.MEDIA_FOLDER_PATH, media.getMediaFolderPath());
                    cvs.put(AudioInfoTable.MEDIA_FOLDER_NAME, media.getMediaFolderName());
                    cvs.put(AudioInfoTable.MEDIA_FOLDER_NAME_PINYIN, media.getMediaFolderNamePinYin());
                    cvs.put(AudioInfoTable.DURATION, media.getDuration());
                    cvs.put(AudioInfoTable.COLLECTED, media.getCollected());
                    cvs.put(AudioInfoTable.COVER_URL, media.getCoverUrl());
                    cvs.put(AudioInfoTable.LYRIC, media.getLyric());
                    cvs.put(AudioInfoTable.CREATE_TIME, media.getCreateTime());
                    cvs.put(AudioInfoTable.UPDATE_TIME, media.getUpdateTime());
                    //EXEC
                    long rowId = mDB.insert(AudioInfoTable.T_NAME, null, cvs);
                    if (rowId > 0) {
                        insertCount++;
                    }
                }
                mDB.setTransactionSuccessful();
            } catch (Exception e) {
                Logs.i(TAG, "insertNewAudios - e :: " + e.getMessage());
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

    /**
     * Update Media Collected Status
     *
     * @param media {@link ProAudio}
     * @return int, The number of rows affected.
     */
    public int updateMediaCollect(ProAudio media) {
        //
        //String table = AudioInfoTable.T_NAME;
        //
        ContentValues values = new ContentValues();
        values.put(AudioInfoTable.COLLECTED, media.getCollected());
        values.put(AudioInfoTable.UPDATE_TIME, media.getUpdateTime());
        //
        String selection = AudioInfoTable.MEDIA_URL + "=?";
        //
        String[] selectionArgs = {media.getMediaUrl()};
        return updateMedias(values, selection, selectionArgs);
    }

    @Override
    public int updateMediaCollect(int type, List mediasToCollect) {
        try {
            //Parse media.
            ProAudio media = (ProAudio) mediasToCollect.get(0);

            //ContentValues
            ContentValues values = new ContentValues();
            values.put(AudioInfoTable.COLLECTED, media.getCollected());
            values.put(AudioInfoTable.UPDATE_TIME, System.currentTimeMillis());
            //selection
            String selection = AudioInfoTable.MEDIA_URL + "=?";
            //selectionArgs
            String[] selectionArgs = {media.getMediaUrl()};
            return updateMedias(values, selection, selectionArgs);
        } catch (Exception e) {
            Logs.i(TAG, "updateMediaCollect >> e: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int clearHistoryCollect() {
        //table
        //values
        ContentValues cvs = new ContentValues();
        cvs.put(AudioInfoTable.COLLECTED, 0);
        cvs.put(AudioInfoTable.UPDATE_TIME, System.currentTimeMillis());
        //whereClause
        String whereClause = null;
        String existDeviceArea = getExistDeviceArea(true);
        if (existDeviceArea != null) {
            whereClause = AudioInfoTable.ROOT_PATH + " not in " + existDeviceArea;
        }
        if (TextUtils.isEmpty(whereClause)) {
            return 0;
        } else {
            whereClause += " and " + AudioInfoTable.COLLECTED + "=1";
        }
        Log.i(TAG, "whereClause : " + whereClause);
        //whereArgs

        //
        int result = 0;
        if (openDB()) {
            Exception exception = null;
            try {
                result = mDB.update(AudioInfoTable.T_NAME, cvs, whereClause, null);
            } catch (Exception e) {
                Logs.i(TAG, "clearHistoryCollect() - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return result;
    }

    @Override
    public synchronized int updateMediaTimeStamp(String mediaUrl) {
        int affectedRowNum = 0;
        if (openDB()) {
            Exception exception = null;
            try {
                //values
                ContentValues values = new ContentValues();
                values.put(AudioInfoTable.UPDATE_TIME, System.currentTimeMillis());
                //whereClause
                String whereClause = AudioInfoTable.MEDIA_URL + "=?";
                //whereArgs
                String[] whereArgs = new String[]{mediaUrl};
                affectedRowNum = mDB.update(AudioInfoTable.T_NAME, values, whereClause, whereArgs);
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
                final String sqlQueryRootPath = "select " + AudioInfoTable.ROOT_PATH
                        + " from " + AudioInfoTable.T_NAME
                        + " where " + AudioInfoTable.STORAGE_ID + "='" + storageId
                        + "' limit 1";
                Logs.i(TAG, "updateMediaPathInfo - SqlQueryRootPath: " + sqlQueryRootPath);
                Cursor cursor = mDB.rawQuery(sqlQueryRootPath, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        oldRootPath = cursor.getString(cursor.getColumnIndex(AudioInfoTable.ROOT_PATH));
                    }
                    cursor.close();
                }
                //如果检测到根路径字符串没有发生改变，则中断执行
                if (TextUtils.isEmpty(oldRootPath) || TextUtils.equals(oldRootPath, rootPath)) {
                    Logs.i(TAG, "updateMediaPathInfo - SqlQueryRootPath: break for root path not changed!!!");
                    return resultCode;
                }

                // 图片路径
                // "/storage/0/Android/data/com.egar.scanner/files/.cover_img_audio/_storage_emulated_0_music_ALIVE.png"
                // 位置: "/storage/0/Android/data/com.egar.scanner/files/.cover_img_audio/"
                // 命名: 媒体文件路径，其中的"/"使用"_"替换掉

                // 根路径命名信息 ，如"_storage_emulated_0_music_ALIVE.png"中的"_storage_emulated_0"
                String oldCoverImgNamePrefix = oldRootPath.replace("/", "_");
                String newCoverImgNamePrefix = rootPath.replace("/", "_");

                //
                final String sqlUpdateRootPath = "update " + AudioInfoTable.T_NAME
                        + " set "
                        + AudioInfoTable.ROOT_PATH + "='" + rootPath + "'"
                        // 替换掉[文件路径]中[根路径]信息
                        + "," + AudioInfoTable.MEDIA_URL
                        + "=replace(" + AudioInfoTable.MEDIA_URL + ",'" + oldRootPath + "','" + rootPath + "')"
                        // 替换掉[图片路径]中[根路径]信息
                        + "," + AudioInfoTable.COVER_URL
                        + "=replace(" + AudioInfoTable.COVER_URL + ",'" + oldRootPath + "','" + rootPath + "')"
                        // 替换掉[图片名称]中[根路径相关字符串]
                        + "," + AudioInfoTable.COVER_URL
                        + "=replace(" + AudioInfoTable.COVER_URL + ",'" + oldCoverImgNamePrefix + "','" + newCoverImgNamePrefix + "')"
                        + " where "
                        + AudioInfoTable.STORAGE_ID + "='" + storageId + "'"
                        + " and "
                        + AudioInfoTable.ROOT_PATH + "!='" + rootPath + "'";
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
                final String[] SRC_COLUMNS = new String[]{AudioInfoTable.ID, AudioInfoTable.TITLE, AudioInfoTable.TITLE_PINYIN, AudioInfoTable.FILE_NAME
                        , AudioInfoTable.ALBUM_ID, AudioInfoTable.ALBUM, AudioInfoTable.ALBUM_PINYIN
                        , AudioInfoTable.ARTIST, AudioInfoTable.ARTIST_PINYIN
                        , AudioInfoTable.STORAGE_ID, AudioInfoTable.ROOT_PATH
                        , AudioInfoTable.MEDIA_URL, AudioInfoTable.MEDIA_FOLDER_PATH, AudioInfoTable.MEDIA_FOLDER_NAME, AudioInfoTable.MEDIA_FOLDER_NAME_PINYIN
                        , AudioInfoTable.DURATION, AudioInfoTable.COLLECTED, AudioInfoTable.COVER_URL, AudioInfoTable.LYRIC
                        , AudioInfoTable.CREATE_TIME, AudioInfoTable.UPDATE_TIME};

                //Table
                String table = AudioInfoTable.T_NAME;
                //columns
                columns = EmptyUtil.isEmpty(columns) ? SRC_COLUMNS : columns;
                //selection
                if (selection == null) {
                    selection = getExistDeviceArea(false);
                } else {
                    String existDeviceArea = getExistDeviceArea(false);
                    if (existDeviceArea != null) {
                        selection += " and " + existDeviceArea;
                    }
                }
                //selectionArgs
                //groupBy
                //having
                //orderBy
                orderBy = TextUtils.isEmpty(orderBy) ? AudioInfoTable.TITLE_PINYIN : orderBy;

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
        int rowId = -1;
        if (openDB()) {
            Exception exception = null;
            try {
                rowId = mDB.delete(AudioInfoTable.T_NAME, selection, selectionArgs);
            } catch (Exception e) {
                Logs.i(TAG, "deleteMedias() >> e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return rowId;
    }

    /**
     * Provider actions - updateMedias
     */
    @Override
    public int updateMedias(@Nullable ContentValues values,
                            @Nullable String selection,
                            @Nullable String[] selectionArgs) {
        Exception exception = null;
        int rowId = 0;
        if (openDB()) {
            try {
                rowId = mDB.update(AudioInfoTable.T_NAME, values, selection, selectionArgs);
            } catch (Exception e) {
                Logs.i(TAG, "updateMediaInfo >> e: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return rowId;
    }

    /**
     * Provider actions - queryMediaSheets
     */
    @Override
    public Cursor queryMediaSheets(@Nullable String[] projection,
                                   @Nullable String selection,
                                   @Nullable String[] selectionArgs,
                                   @Nullable String sortOrder) {
        Cursor cursor = null;
        if (openDB()) {
            Exception exception = null;
            try {
                String table = AudioSheetTable.T_NAME;
                String[] columns = new String[]{AudioSheetTable.ID, AudioSheetTable.TITLE, AudioSheetTable.TITLE_PINYIN
                        , AudioSheetTable.CREATE_TIME, AudioSheetTable.UPDATE_TIME};
                cursor = mDB.query(table, EmptyUtil.isEmpty(projection) ? columns : projection, selection, selectionArgs, null, null, sortOrder);
            } catch (Exception e) {
                Logs.i(TAG, "Cursor queryMediaSheets - e :: " + e.getMessage());
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
     * Provider actions - insertNewMediaSheet
     */
    @Override
    public long insertNewMediaSheets(ContentValues values) {
        long rowId = -1;
        if (openDB()) {
            Exception exception = null;
            try {
                rowId = mDB.insert(AudioSheetTable.T_NAME, null, values);
            } catch (Exception e) {
                Logs.i(TAG, "Cursor insertNewMediaSheet - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return rowId;
    }

    /**
     * Provider actions - updateMediaSheet
     */
    @Override
    public long updateMediaSheets(@Nullable ContentValues values,
                                  @Nullable String selection,
                                  @Nullable String[] selectionArgs) {
        int rowId = 0;
        if (openDB()) {
            Exception exception = null;
            try {
                rowId = mDB.update(AudioSheetTable.T_NAME, values, selection, selectionArgs);
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
        return rowId;
    }

    /**
     * Provider actions - queryMediaSheetMapInfos
     */
    @Override
    public Cursor queryMediaSheetMapInfos(@Nullable String[] projection,
                                          @Nullable String selection,
                                          @Nullable String[] selectionArgs,
                                          @Nullable String sortOrder) {
        Cursor cursor = null;
        if (openDB()) {
            Exception exception = null;
            try {
                String table = AudioSheetMapInfoTable.T_NAME;
                String[] columns = new String[]{AudioSheetMapInfoTable.ID, AudioSheetMapInfoTable.SHEET_ID, AudioSheetMapInfoTable.MEDIA_URL
                        , AudioSheetMapInfoTable.CREATE_TIME, AudioSheetMapInfoTable.UPDATE_TIME};
                cursor = mDB.query(table, EmptyUtil.isEmpty(projection) ? columns : projection, selection, selectionArgs, null, null, sortOrder);
            } catch (Exception e) {
                Logs.i(TAG, "Cursor queryMediaSheetMapInfos - e :: " + e.getMessage());
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
     * Provider actions - insertNewMediaSheetMapInfo
     */
    @Override
    public long insertNewMediaSheetMapInfos(ContentValues values) {
        long rowId = -1;
        if (openDB()) {
            Exception exception = null;
            try {
                //Query exist
                String sql = "select " + AudioSheetMapInfoTable.SHEET_ID
                        + " from " + AudioSheetMapInfoTable.T_NAME
                        + " where " + AudioSheetMapInfoTable.SHEET_ID + "=" + values.get(AudioSheetMapInfoTable.SHEET_ID)
                        + " and " + AudioSheetMapInfoTable.MEDIA_URL + "='" + values.get(AudioSheetMapInfoTable.MEDIA_URL) + "'";
                Cursor cursor = mDB.rawQuery(sql, null);
                if (cursor.getCount() <= 0) {
                    rowId = mDB.insert(AudioSheetMapInfoTable.T_NAME, null, values);
                }
                cursor.close();
            } catch (Exception e) {
                Logs.i(TAG, "Cursor insertNewMediaSheetMapInfo - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return rowId;
    }

    /**
     * Provider actions - deleteMediaSheetMapInfos
     */
    @Override
    public long deleteMediaSheetMapInfos(String where, String[] whereArgs) {
        long rowId = -1;
        if (openDB()) {
            Exception exception = null;
            try {
                rowId = mDB.delete(AudioSheetMapInfoTable.T_NAME, where, whereArgs);
            } catch (Exception e) {
                Logs.i(TAG, "Cursor insertNewMediaSheetMapInfo - e :: " + e.getMessage());
                exception = e;
                e.printStackTrace();
            } finally {
                if (exception != null) {
                    closeDB();
                }
            }
        }
        return rowId;
    }
}
