package juns.lib.media.provider.image;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import juns.lib.android.utils.Logs;
import juns.lib.media.db.base.BaseDBManager;
import juns.lib.media.db.manager.ImageDBManager;
import juns.lib.media.provider.image.ImageProviderInfo.ImageUriCodes;
import juns.lib.media.provider.image.ImageProviderInfo.ImageUriPaths;

public class LocalImageProvider extends ContentProvider {
    //TAG
    private static final String TAG = "LocalImageProvider";

    /**
     * Media database manger.
     */
    private BaseDBManager mDBManager;

    //
    static UriMatcher sMatcher;

    @Override
    public boolean onCreate() {
        Logs.i(TAG, "onCreate()");
        addUris();
        mDBManager = ImageDBManager.instance(getContext());
        return false;
    }

    private void addUris() {
        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //Query
        sMatcher.addURI(ImageUriPaths.AUTHORITY,
                ImageUriPaths.PATH_MEDIA_INFO_QUERY_COUNT,
                ImageUriCodes.CODE_MEDIA_INFO_QUERY_COUNT);
        sMatcher.addURI(ImageUriPaths.AUTHORITY,
                ImageUriPaths.PATH_MEDIA_INFO_QUERY_DISTINCT_COLS,
                ImageUriCodes.CODE_MEDIA_INFO_QUERY_DISTINCT_COLS);
        sMatcher.addURI(ImageUriPaths.AUTHORITY,
                ImageUriPaths.PATH_MEDIA_INFO_QUERY_ALL,
                ImageUriCodes.CODE_MEDIA_INFO_QUERY_ALL);
        //使用"*"去匹配任意的文本数据，或者使用"#"去匹配任意的数字
        sMatcher.addURI(ImageUriPaths.AUTHORITY,
                ImageUriPaths.PATH_MEDIA_INFO_QUERY_ITEM + "/#",
                ImageUriCodes.CODE_MEDIA_INFO_QUERY_ITEM);
        //Insert
        sMatcher.addURI(ImageUriPaths.AUTHORITY,
                ImageUriPaths.PATH_MEDIA_INFO_INSERT,
                ImageUriCodes.CODE_MEDIA_INFO_INSERT);
        //Delete
        sMatcher.addURI(ImageUriPaths.AUTHORITY,
                ImageUriPaths.PATH_MEDIA_INFO_DELETE,
                ImageUriCodes.CODE_MEDIA_INFO_DELETE);
        //Update
        sMatcher.addURI(ImageUriPaths.AUTHORITY,
                ImageUriPaths.PATH_MEDIA_INFO_UPDATE,
                ImageUriCodes.CODE_MEDIA_INFO_UPDATE);
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
            case ImageUriCodes.CODE_MEDIA_INFO_QUERY_ALL:
                return "vnd.android.cursor.dir/vnd." + ImageUriPaths.PATH_MEDIA_INFO_QUERY_ALL;
            case ImageUriCodes.CODE_MEDIA_INFO_QUERY_ITEM:
                return "vnd.android.cursor.item/vnd." + ImageUriPaths.PATH_MEDIA_INFO_QUERY_ITEM;
        }
        return null;
    }

    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Logs.i(TAG, "query(" + uri + ")");
        Cursor cursor = null;
        switch (sMatcher.match(uri)) {
            case ImageUriCodes.CODE_MEDIA_INFO_QUERY_COUNT:
                break;
            case ImageUriCodes.CODE_MEDIA_INFO_QUERY_DISTINCT_COLS:
                if (projection != null && projection.length > 0) {
                    cursor = mDBManager.queryMedias(projection, selection, selectionArgs, projection[0], null, sortOrder);
                } else {
                    cursor = mDBManager.queryMedias(projection, selection, selectionArgs, null, null, sortOrder);
                }
                break;
            case ImageUriCodes.CODE_MEDIA_INFO_QUERY_ALL:
                cursor = mDBManager.queryMedias(projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ImageUriCodes.CODE_MEDIA_INFO_QUERY_ITEM:
                break;
        }
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (sMatcher.match(uri) == ImageUriCodes.CODE_MEDIA_INFO_INSERT) {
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (sMatcher.match(uri) == ImageUriCodes.CODE_MEDIA_INFO_DELETE) {
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (sMatcher.match(uri) == ImageUriCodes.CODE_MEDIA_INFO_UPDATE) {
        }
        return 0;
    }
}
