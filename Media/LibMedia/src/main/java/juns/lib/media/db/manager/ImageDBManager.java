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
import juns.lib.media.bean.ProImage;
import juns.lib.media.db.base.BaseDBManager;
import juns.lib.media.db.helper.ImageDBHelper;
import juns.lib.media.db.tables.ImageTables.ImageInfoTable;
import juns.lib.media.flags.MediaType;
import juns.lib.media.provider.image.ImageParser;
import juns.lib.media.utils.ScannerFileUtils;

/**
 * 该类用来处理数据库操作
 *
 * @author Jun.Wang
 */
public class ImageDBManager extends BaseDBManager {
    // TAG
    private static final String TAG = "ImageDBManager";

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
    private static ImageDBManager mDBManager;

    private ImageDBManager(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static ImageDBManager instance(Context context) {
        if (mDBManager == null) {
            synchronized (ImageDBManager.class) {
                mDBManager = new ImageDBManager(context);
            }
        }
        return mDBManager;
    }

    @Override
    public void setDbPath(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) {
            mDbPath = ScannerFileUtils.getDbFilePath(MediaType.IMAGE);
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
                SQLiteOpenHelper helper = new ImageDBHelper(mAppContext, mDbPath);
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
                String sql = "select count(" + ImageInfoTable.MEDIA_URL + ") as TOTAL_COUNT from " + ImageInfoTable.T_NAME;
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
        HashMap<String, ProImage> mapMedias = new HashMap<>();

        //Get where
        String selection = getExistDeviceArea(false);
        //Query
        Exception exception = null;
        Cursor cursor = null;
        try {
            cursor = queryMedias(null, selection, null, null, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ProImage media = ImageParser.parseFromCursor(cursor);
                mapMedias.put(media.getMediaUrl(), media);
            }
        } catch (Exception e) {
            Logs.i(TAG, "Cursor queryMedias - e :: " + e.getMessage());
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
    public synchronized int insertNewMedias(final List<? extends MediaBase> listMedias) {
        int insertCount = 0;
        if (listMedias != null && openDB()) {
            Exception exception = null;
            try {
                mDB.beginTransaction();
                for (MediaBase mediaBase : listMedias) {
                    //Convert base to real.
                    ProImage media = (ProImage) mediaBase;
                    //Construct key-value
                    ContentValues cvs = new ContentValues();
                    //                    cvs.put(ImageInfoTable.ID, media.getId());
                    cvs.put(ImageInfoTable.TITLE, media.getTitle());
                    cvs.put(ImageInfoTable.TITLE_PINYIN, media.getTitlePinYin());
                    cvs.put(ImageInfoTable.FILE_NAME, media.getFileName());
                    cvs.put(ImageInfoTable.STORAGE_ID, media.getStorageId());
                    cvs.put(ImageInfoTable.ROOT_PATH, media.getRootPath());
                    cvs.put(ImageInfoTable.MEDIA_URL, media.getMediaUrl());
                    cvs.put(ImageInfoTable.MEDIA_FOLDER_PATH, media.getMediaFolderPath());
                    cvs.put(ImageInfoTable.MEDIA_FOLDER_NAME, media.getMediaFolderName());
                    cvs.put(ImageInfoTable.MEDIA_FOLDER_NAME_PINYIN, media.getMediaFolderNamePinYin());
                    cvs.put(ImageInfoTable.CREATE_TIME, media.getCreateTime());
                    cvs.put(ImageInfoTable.UPDATE_TIME, media.getUpdateTime());
                    long rowId = mDB.insert(ImageInfoTable.T_NAME, null, cvs);
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
    public synchronized int updateMediaTimeStamp(String mediaUrl) {
        int affectedRowNum = 0;
        if (openDB()) {
            Exception exception = null;
            try {
                //values
                ContentValues values = new ContentValues();
                values.put(ImageInfoTable.UPDATE_TIME, System.currentTimeMillis());
                //whereClause
                String whereClause = ImageInfoTable.MEDIA_URL + "=?";
                //whereArgs
                String[] whereArgs = new String[]{mediaUrl};
                affectedRowNum = mDB.update(ImageInfoTable.T_NAME, values, whereClause, whereArgs);
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
                final String sqlQueryRootPath = "select " + ImageInfoTable.ROOT_PATH
                        + " from " + ImageInfoTable.T_NAME
                        + " where " + ImageInfoTable.STORAGE_ID + "='" + storageId + "'  limit 1";
                Logs.i(TAG, "updateMediaPathInfo - SqlQueryRootPath: " + sqlQueryRootPath);
                Cursor cursor = mDB.rawQuery(sqlQueryRootPath, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        oldRootPath = cursor.getString(cursor.getColumnIndex(ImageInfoTable.ROOT_PATH));
                    }
                    cursor.close();
                }
                //如果检测到根路径字符串没有发生改变，则中断执行
                if (TextUtils.isEmpty(oldRootPath) || TextUtils.equals(oldRootPath, rootPath)) {
                    Logs.i(TAG, "updateMediaPathInfo - SqlQueryRootPath: break for root path not changed!!!");
                    return resultCode;
                }

                //
                final String sqlUpdateRootPath = "update " + ImageInfoTable.T_NAME
                        + " set "
                        + ImageInfoTable.ROOT_PATH + "='" + rootPath + "'"
                        + "," + ImageInfoTable.MEDIA_URL
                        + "=replace(" + ImageInfoTable.MEDIA_URL + ",'" + oldRootPath + "','" + rootPath + "')"
                        + " where "
                        + ImageInfoTable.STORAGE_ID + "='" + storageId + "'"
                        + " and "
                        + ImageInfoTable.ROOT_PATH + "!='" + rootPath + "'";
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
                String[] SRC_COLUMNS = new String[]{ImageInfoTable.ID, ImageInfoTable.TITLE, ImageInfoTable.TITLE_PINYIN, ImageInfoTable.FILE_NAME
                        , ImageInfoTable.STORAGE_ID, ImageInfoTable.ROOT_PATH
                        , ImageInfoTable.MEDIA_URL, ImageInfoTable.MEDIA_FOLDER_PATH, ImageInfoTable.MEDIA_FOLDER_NAME, ImageInfoTable.MEDIA_FOLDER_NAME_PINYIN
                        , ImageInfoTable.CREATE_TIME, ImageInfoTable.UPDATE_TIME};

                //Table
                String table = ImageInfoTable.T_NAME;
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
                orderBy = TextUtils.isEmpty(orderBy) ? ImageInfoTable.TITLE_PINYIN : orderBy;

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
