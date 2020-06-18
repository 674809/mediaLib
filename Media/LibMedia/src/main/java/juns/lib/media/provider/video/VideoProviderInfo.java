package juns.lib.media.provider.video;

import android.net.Uri;

/**
 * Video Provider information
 * <p>Supply uri information</p>
 *
 * @author Jun.Wang
 */
public final class VideoProviderInfo {
    /**
     * Uri prefix
     */
    private static final String URI_PREFIX = "content://";

    public interface VideoUriPaths {
        // AUTHORITY
        String AUTHORITY = "com.egar.scanner.provider.video";

        //-- Media --
        //Query
        String PATH_MEDIA_INFO_QUERY_COUNT = "videoInfo/queryCount";
        String PATH_MEDIA_INFO_QUERY_DISTINCT_COLS = "videoInfo/queryDistinctCols";
        String PATH_MEDIA_INFO_QUERY_ALL = "videoInfo/queryAll";
        String PATH_MEDIA_INFO_QUERY_ITEM = "videoInfo/query";
        //Insert
        String PATH_MEDIA_INFO_INSERT = "videoInfo/insert";
        //Delete
        String PATH_MEDIA_INFO_DELETE = "videoInfo/delete";
        //Update
        String PATH_MEDIA_INFO_UPDATE = "videoInfo/update";
    }

    public interface VideoUriCodes {
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
        return Uri.parse(URI_PREFIX + VideoUriPaths.AUTHORITY + "/" + VideoUriPaths.PATH_MEDIA_INFO_QUERY_COUNT);
    }

    /**
     * Media - query all
     */
    public static Uri getUriMediaInfoQueryDistinctCols() {
        return Uri.parse(URI_PREFIX + VideoUriPaths.AUTHORITY + "/" + VideoUriPaths.PATH_MEDIA_INFO_QUERY_DISTINCT_COLS);
    }

    public static Uri getUriMediaInfoQueryAll() {
        return Uri.parse(URI_PREFIX + VideoUriPaths.AUTHORITY + "/" + VideoUriPaths.PATH_MEDIA_INFO_QUERY_ALL);
    }

    public static Uri getUriMediaInfoQueryItem() {
        return Uri.parse(URI_PREFIX + VideoUriPaths.AUTHORITY + "/" + VideoUriPaths.PATH_MEDIA_INFO_QUERY_ITEM);
    }

    public static Uri getUriMediaInfoInsert() {
        return Uri.parse(URI_PREFIX + VideoUriPaths.AUTHORITY + "/" + VideoUriPaths.PATH_MEDIA_INFO_INSERT + "/" + VideoUriCodes.CODE_MEDIA_INFO_INSERT);
    }

    public static Uri getUriMediaInfoDelete() {
        return Uri.parse(URI_PREFIX + VideoUriPaths.AUTHORITY + "/" + VideoUriPaths.PATH_MEDIA_INFO_DELETE + "/" + VideoUriCodes.CODE_MEDIA_INFO_DELETE);
    }

    public static Uri getUriMediaInfoUpdate() {
        return Uri.parse(URI_PREFIX + VideoUriPaths.AUTHORITY + "/" + VideoUriPaths.PATH_MEDIA_INFO_UPDATE + "/" + VideoUriCodes.CODE_MEDIA_INFO_UPDATE);
    }
}
