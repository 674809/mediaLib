package juns.lib.media.provider.audio;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.media.db.base.BaseDBManager;
import juns.lib.media.db.manager.AudioDBManager;
import juns.lib.media.provider.audio.AudioProviderInfo.AudioUriCodes;
import juns.lib.media.provider.audio.AudioProviderInfo.AudioUriPaths;

public class LocalAudioProvider extends ContentProvider {
    //TAG
    private static final String TAG = "LocalAudioProvider";

    /**
     * Media database manger.
     */
    private BaseDBManager mDBManager;

    /**
     * Int values in flash.
     */
    private Map<String, Integer> mMapIntValues;

    /**
     * Boolean values in flash.
     */
    private Map<String, Boolean> mMapBoolValues;

    // UriMatcher
    static UriMatcher sMatcher;

    @Override
    public boolean onCreate() {
        Logs.i(TAG, "onCreate()");
        addUris();
        mDBManager = AudioDBManager.instance(getContext());
        return false;
    }

    private void addUris() {
        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //-- Media --
        //Query
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_QUERY_COUNT,
                AudioUriCodes.CODE_MEDIA_INFO_QUERY_COUNT);
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_QUERY_DISTINCT_COLS,
                AudioUriCodes.CODE_MEDIA_INFO_QUERY_DISTINCT_COLS);
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_QUERY_ALL,
                AudioUriCodes.CODE_MEDIA_INFO_QUERY_ALL);
        //使用"*"去匹配任意的文本数据，或者使用"#"去匹配任意的数字
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_QUERY_ITEM + "/#",
                AudioUriCodes.CODE_MEDIA_INFO_QUERY_ITEM);
        //Insert
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_INSERT,
                AudioUriCodes.CODE_MEDIA_INFO_INSERT);
        //Delete
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_DELETE,
                AudioUriCodes.CODE_MEDIA_INFO_DELETE);
        //Update
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_UPDATE,
                AudioUriCodes.CODE_MEDIA_INFO_UPDATE);

        //-- Media Sheet --
        //Query
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_SHEET_QUERY_ALL,
                AudioUriCodes.CODE_MEDIA_SHEET_QUERY_ALL);
        //使用"*"去匹配任意的文本数据，或者使用"#"去匹配任意的数字
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_SHEET_QUERY_ITEM + "/#",
                AudioUriCodes.CODE_MEDIA_SHEET_QUERY_ALL);
        //Insert
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_SHEET_INSERT,
                AudioUriCodes.CODE_MEDIA_SHEET_INSERT);
        //Delete
        //Update
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_SHEET_UPDATE,
                AudioUriCodes.CODE_MEDIA_SHEET_UPDATE);

        //-- Media Sheet map information --
        //Query
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_SHEET_MAP_INFO_QUERY_ALL,
                AudioUriCodes.CODE_MEDIA_SHEET_MAP_INFO_QUERY_ALL);
        //Insert
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_SHEET_MAP_INFO_INSERT,
                AudioUriCodes.CODE_MEDIA_SHEET_MAP_INFO_INSERT);
        //Delete
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_SHEET_MAP_INFO_DELETE,
                AudioUriCodes.CODE_MEDIA_SHEET_MAP_INFO_DELETE);
        //Update

        // -- Media int values in flash --
        //Query
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_GET_INT,
                AudioUriCodes.CODE_MEDIA_INFO_GET_INT);
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_GET_BOOL,
                AudioUriCodes.CODE_MEDIA_INFO_GET_BOOL);
        //Insert
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_SET_INT,
                AudioUriCodes.CODE_MEDIA_INFO_SET_INT);
        sMatcher.addURI(AudioUriPaths.AUTHORITY,
                AudioUriPaths.PATH_MEDIA_INFO_SET_BOOL,
                AudioUriCodes.CODE_MEDIA_INFO_SET_BOOL);
        //Delete
        //Update
    }

    /**
     * MIME 获取
     * <p>一个内容 URI 对应的 MIME 字符串主要有 3 部分组成。Android 对着 3 部分做了如下格式规定。</p>
     * <p>1. 必须以 vnd 开头</p>
     * <p>2. 如果内容 URI 以 路径结尾，则接 android.cursor.dir/；
     * 如果以 id 结尾，则接 andriod.cursor.item/</p>
     * <p>3. 最后 接上 vnd.<authority>.path</p>
     * <p>Example : content://com.example.tnt.bookproject/Book -> vnd.android.cursor.dir/vnd.com.example.tnt.bookproject.Book</p>
     * <p>Example : content://com.example.tnt.bookproject/Book/1 -> vnd.android.cursor.item/vnd.com.example.tnt.bookproject.Book</p>
     *
     * @param uri Uri包装
     * @return String MIME字符串
     */
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sMatcher.match(uri)) {
            case AudioUriCodes.CODE_MEDIA_INFO_QUERY_ALL:
                return "vnd.android.cursor.dir/vnd." + AudioUriPaths.PATH_MEDIA_INFO_QUERY_ALL;
            case AudioUriCodes.CODE_MEDIA_INFO_QUERY_ITEM:
                return "vnd.android.cursor.item/vnd." + AudioUriPaths.PATH_MEDIA_INFO_QUERY_ITEM;
        }
        return null;
    }

    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Logs.i(TAG, "query(" + uri + "," + projection + "," + selection + "," + selectionArgs + "," + sortOrder + ")");
        Cursor cursor = null;
        switch (sMatcher.match(uri)) {
            case AudioUriCodes.CODE_MEDIA_INFO_QUERY_COUNT:
                break;
            case AudioUriCodes.CODE_MEDIA_INFO_QUERY_DISTINCT_COLS:
                if (projection != null && projection.length > 0) {
                    cursor = mDBManager.queryMedias(projection, selection, selectionArgs, projection[0], null, sortOrder);
                } else {
                    cursor = mDBManager.queryMedias(projection, selection, selectionArgs, null, null, sortOrder);
                }
                break;
            case AudioUriCodes.CODE_MEDIA_INFO_QUERY_ALL:
                cursor = mDBManager.queryMedias(projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case AudioUriCodes.CODE_MEDIA_INFO_QUERY_ITEM:
                break;
            case AudioUriCodes.CODE_MEDIA_SHEET_QUERY_ALL:
                cursor = mDBManager.queryMediaSheets(projection, selection, selectionArgs, sortOrder);
                break;
            case AudioUriCodes.CODE_MEDIA_SHEET_MAP_INFO_QUERY_ALL:
                cursor = mDBManager.queryMediaSheetMapInfos(projection, selection, selectionArgs, sortOrder);
                break;
        }
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri rowUri = null;
        switch (sMatcher.match(uri)) {
            case AudioUriCodes.CODE_MEDIA_SHEET_INSERT:
                long rowId = mDBManager.insertNewMediaSheets(values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(AudioProviderInfo.getUriMediaSheetQueryItem(), rowId);
                }
                break;
            case AudioUriCodes.CODE_MEDIA_SHEET_MAP_INFO_INSERT:
                rowId = mDBManager.insertNewMediaSheetMapInfos(values);
                if (rowId > 0) {
                    rowUri = ContentUris.withAppendedId(AudioProviderInfo.getUriMediaSheetMapInfoQueryItem(), rowId);
                }
                break;
        }
        return rowUri;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int rowId = 0;
        switch (sMatcher.match(uri)) {
            case AudioUriCodes.CODE_MEDIA_INFO_DELETE:
                rowId = mDBManager.deleteMedias(selection, selectionArgs);
                break;
            case AudioUriCodes.CODE_MEDIA_SHEET_MAP_INFO_DELETE:
                rowId = (int) mDBManager.deleteMediaSheetMapInfos(selection, selectionArgs);
                break;
        }
        return rowId;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int rowId = 0;
        switch (sMatcher.match(uri)) {
            case AudioUriCodes.CODE_MEDIA_INFO_UPDATE:
                rowId = mDBManager.updateMedias(values, selection, selectionArgs);
                break;
            case AudioUriCodes.CODE_MEDIA_SHEET_UPDATE:
                rowId = (int) mDBManager.updateMediaSheets(values, selection, selectionArgs);
                break;
        }
        return rowId;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method,
                       @Nullable String name,
                       @Nullable Bundle bundle) {
        //
        if (TextUtils.isEmpty(method) || TextUtils.isEmpty(name)) {
            return null;
        }

        //
        Bundle resBundle = null;
        switch (method) {
            //int
            case "setInt":
                if (bundle != null) {
                    int value = bundle.getInt(name, -999);
                    if (value != -999) {
                        if (mMapIntValues == null) {
                            mMapIntValues = new HashMap<>();
                        }
                        mMapIntValues.put(name, value);
                    }
                }
                break;
            case "getInt":
                if (!TextUtils.isEmpty(name) && mMapIntValues != null) {
                    Integer value = mMapIntValues.get(name);
                    if (value != null) {
                        resBundle = new Bundle();
                        resBundle.putInt(name, value);
                    }
                }
                break;

            //int
            case "setBool":
                if (bundle != null) {
                    boolean value = bundle.getBoolean(name, false);
                    if (mMapBoolValues == null) {
                        mMapBoolValues = new HashMap<>();
                    }
                    mMapBoolValues.put(name, value);
                }
                break;
            case "getBool":
                if (!TextUtils.isEmpty(name) && mMapBoolValues != null) {
                    Boolean value = mMapBoolValues.get(name);
                    if (value != null) {
                        resBundle = new Bundle();
                        resBundle.putBoolean(name, value);
                    }
                }
                break;
        }
        return resBundle;
    }
}
