package juns.lib.media.provider.audio;

import android.net.Uri;

/**
 * Audio Provider information
 * <p>Supply uri information</p>
 *
 * @author Jun.Wang
 */
public final class AudioProviderInfo {
    /**
     * Uri prefix
     */
    private static final String URI_PREFIX = "content://";

    public interface AudioUriPaths {
        // AUTHORITY
        String AUTHORITY = "com.egar.scanner.provider.audio";

        //-- Media --
        //Query
        String PATH_MEDIA_INFO_QUERY_COUNT = "audioInfo/queryCount";
        String PATH_MEDIA_INFO_QUERY_DISTINCT_COLS = "audioInfo/queryDistinctCols";
        String PATH_MEDIA_INFO_QUERY_ALL = "audioInfo/queryAll";
        String PATH_MEDIA_INFO_QUERY_ITEM = "audioInfo/query";
        //Insert
        String PATH_MEDIA_INFO_INSERT = "audioInfo/insert";
        //Delete
        String PATH_MEDIA_INFO_DELETE = "audioInfo/delete";
        //Update
        String PATH_MEDIA_INFO_UPDATE = "audioInfo/update";

        //-- Media sheet --
        //Query
        String PATH_MEDIA_SHEET_QUERY_ALL = "audioSheet/queryAll";
        String PATH_MEDIA_SHEET_QUERY_ITEM = "audioSheet/query";
        //Insert
        String PATH_MEDIA_SHEET_INSERT = "audioSheet/insert";
        //Delete
        //Update
        String PATH_MEDIA_SHEET_UPDATE = "audioSheet/update";

        //-- Media sheet map information --
        //Query
        String PATH_MEDIA_SHEET_MAP_INFO_QUERY_ALL = "audioSheetMapInfo/queryAll";
        String PATH_MEDIA_SHEET_MAP_INFO_QUERY_ITEM = "audioSheetMapInfo/queryItem";
        //Insert
        String PATH_MEDIA_SHEET_MAP_INFO_INSERT = "audioSheetMapInfo/insert";
        //Delete
        String PATH_MEDIA_SHEET_MAP_INFO_DELETE = "audioSheetMapInfo/delete";
        //Update

        //-- Get/Set INT/BOOL --
        //Query
        String PATH_MEDIA_INFO_GET_INT = "audioInfo/getInt";
        String PATH_MEDIA_INFO_GET_BOOL = "audioInfo/getBool";
        //Insert
        String PATH_MEDIA_INFO_SET_INT = "audioInfo/setInt";
        String PATH_MEDIA_INFO_SET_BOOL = "audioInfo/setBool";
    }

    public interface AudioUriCodes {
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

        //-- Media sheet --
        //Query
        int CODE_MEDIA_SHEET_QUERY_ALL = 200;
        int CODE_MEDIA_SHEET_QUERY_ITEM = 201;
        //Insert
        int CODE_MEDIA_SHEET_INSERT = 250;
        //Delete
        //Update
        int CODE_MEDIA_SHEET_UPDATE = 350;

        //-- Media sheet map information --
        //Query
        int CODE_MEDIA_SHEET_MAP_INFO_QUERY_ALL = 400;
        int CODE_MEDIA_SHEET_MAP_INFO_QUERY_ITEM = 401;
        //Insert
        int CODE_MEDIA_SHEET_MAP_INFO_INSERT = 450;
        //Delete
        int CODE_MEDIA_SHEET_MAP_INFO_DELETE = 500;
        //Update

        //-- Get/Set INT/BOOL --
        //Query
        int CODE_MEDIA_INFO_GET_INT = 600;
        int CODE_MEDIA_INFO_GET_BOOL = 601;
        //Insert
        int CODE_MEDIA_INFO_SET_INT = 700;
        int CODE_MEDIA_INFO_SET_BOOL = 701;
    }

    /**
     * Media - query medias count
     */
    public static Uri getUriMediaInfoQueryCount() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_QUERY_COUNT);
    }

    /**
     * Media - query all
     */
    public static Uri getUriMediaInfoQueryDistinctCols() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_QUERY_DISTINCT_COLS);
    }

    /**
     * Media - query all
     */
    public static Uri getUriMediaInfoQueryAll() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_QUERY_ALL);
    }

    /**
     * Media - query item
     */
    public static Uri getUriMediaInfoQueryItem() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_QUERY_ITEM);
    }

    /**
     * Media - insert
     */
    public static Uri getUriMediaInfoInsert() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_INSERT);
    }

    /**
     * Media - delete
     */
    public static Uri getUriMediaInfoDelete() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_DELETE);
    }

    /**
     * Media - update
     */
    public static Uri getUriMediaInfoUpdate() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_UPDATE);
    }


    /**
     * Media sheet - query all
     */
    public static Uri getUriMediaSheetQueryAll() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_QUERY_ALL);
    }

    /**
     * Media sheet - query item
     */
    public static Uri getUriMediaSheetQueryItem() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_QUERY_ITEM);
    }

    /**
     * Media sheet - insert
     */
    public static Uri getUriMediaSheetInsert() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_INSERT);
    }

    /**
     * Media sheet - update
     */
    public static Uri getUriMediaSheetUpdate() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_UPDATE);
    }

    /**
     * Media sheet map information - query all
     */
    public static Uri getUriMediaSheetMapInfoQueryAll() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_MAP_INFO_QUERY_ALL);
    }

    /**
     * Media sheet map information - query item
     */
    public static Uri getUriMediaSheetMapInfoQueryItem() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_MAP_INFO_QUERY_ITEM);
    }

    /**
     * Media sheet map information - insert
     */
    public static Uri getUriMediaSheetMapInfoInsert() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_MAP_INFO_INSERT);
    }

    /**
     * Media sheet map information - delete
     */
    public static Uri getUriMediaSheetMapInfoDelete() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_SHEET_MAP_INFO_DELETE);
    }

    /**
     * Media - Uri - getInt
     */
    public static Uri getUriGetInt() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_GET_INT);
    }

    /**
     * Media - Uri - setInt
     */
    public static Uri getUriSetInt() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_SET_INT);
    }

    /**
     * Media - Uri - getBool
     */
    public static Uri getUriGetBool() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_GET_BOOL);
    }

    /**
     * Media - Uri - setBool
     */
    public static Uri getUriSetBool() {
        return Uri.parse(URI_PREFIX + AudioUriPaths.AUTHORITY + "/" + AudioUriPaths.PATH_MEDIA_INFO_SET_BOOL);
    }
}
