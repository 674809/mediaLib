package juns.lib.media.provider.image;

import android.net.Uri;

/**
 * Image Provider information
 * <p>Supply uri information</p>
 *
 * @author Jun.Wang
 */
public final class ImageProviderInfo {
    /**
     * Uri prefix
     */
    private static final String URI_PREFIX = "content://";

    public interface ImageUriPaths {
        // AUTHORITY
        String AUTHORITY = "com.egar.scanner.provider.image";

        //-- Media --
        //Query
        String PATH_MEDIA_INFO_QUERY_COUNT = "imageInfo/queryCount";
        String PATH_MEDIA_INFO_QUERY_DISTINCT_COLS = "audioInfo/queryDistinctCols";
        String PATH_MEDIA_INFO_QUERY_ALL = "imageInfo/queryAll";
        String PATH_MEDIA_INFO_QUERY_ITEM = "imageInfo/query";
        //Insert
        String PATH_MEDIA_INFO_INSERT = "imageInfo/insert";
        //Delete
        String PATH_MEDIA_INFO_DELETE = "imageInfo/delete";
        //Update
        String PATH_MEDIA_INFO_UPDATE = "imageInfo/update";
    }

    /**
     * Uri 信息
     */
    public interface ImageUriCodes {
        //-- Media --
        //Query
        int CODE_MEDIA_INFO_QUERY_COUNT = 1;
        int CODE_MEDIA_INFO_QUERY_DISTINCT_COLS = 2;
        int CODE_MEDIA_INFO_QUERY_ALL = 3;
        int CODE_MEDIA_INFO_QUERY_ITEM = 4;
        //Insert
        int CODE_MEDIA_INFO_INSERT = 50;
        //Delete
        int CODE_MEDIA_INFO_DELETE = 100;
        //Update
        int CODE_MEDIA_INFO_UPDATE = 150;
    }

    public static Uri getUriMediaInfoQueryCount() {
        return Uri.parse(URI_PREFIX + ImageUriPaths.AUTHORITY + "/" + ImageUriPaths.PATH_MEDIA_INFO_QUERY_COUNT);
    }

    /**
     * Media - query all
     */
    public static Uri getUriMediaInfoQueryDistinctCols() {
        return Uri.parse(URI_PREFIX + ImageUriPaths.AUTHORITY + "/" + ImageUriPaths.PATH_MEDIA_INFO_QUERY_DISTINCT_COLS);
    }

    public static Uri getUriMediaInfoQueryAll() {
        return Uri.parse(URI_PREFIX + ImageUriPaths.AUTHORITY + "/" + ImageUriPaths.PATH_MEDIA_INFO_QUERY_ALL);
    }

    public static Uri getUriMediaInfoQueryItem() {
        return Uri.parse(ImageUriPaths.AUTHORITY + "/" + ImageUriPaths.PATH_MEDIA_INFO_QUERY_ITEM);
    }

    public static Uri getUriMediaInfoInsert() {
        return Uri.parse(ImageUriPaths.AUTHORITY + "/" + ImageUriPaths.PATH_MEDIA_INFO_INSERT + "/" + ImageUriCodes.CODE_MEDIA_INFO_INSERT);
    }

    public static Uri getUriMediaInfoDelete() {
        return Uri.parse(ImageUriPaths.AUTHORITY + "/" + ImageUriPaths.PATH_MEDIA_INFO_DELETE + "/" + ImageUriCodes.CODE_MEDIA_INFO_DELETE);
    }

    public static Uri getUriMediaInfoUpdate() {
        return Uri.parse(ImageUriPaths.AUTHORITY + "/" + ImageUriPaths.PATH_MEDIA_INFO_UPDATE + "/" + ImageUriCodes.CODE_MEDIA_INFO_UPDATE);
    }
}
