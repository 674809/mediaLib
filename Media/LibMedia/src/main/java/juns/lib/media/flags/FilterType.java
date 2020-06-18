package juns.lib.media.flags;

/**
 * 媒体排序方式
 */
public final class FilterType {
    public static final int NOTHING = 0;
    public static final int FOLDER_NAME = 1;
    public static final int MEDIA_NAME = 2;
    public static final int ARTIST_NAME = 3;
    public static final int ALBUM_NAME = 4;

    /**
     * Get description.
     */
    public static String desc(int type) {
        switch (type) {
            case NOTHING:
                return "NOTHING";
            case FOLDER_NAME:
                return "FOLDER_NAME";
            case MEDIA_NAME:
                return "MEDIA_NAME";
            case ARTIST_NAME:
                return "ARTIST_NAME";
            case ALBUM_NAME:
                return "ALBUM_NAME";
            default:
                return "";
        }
    }
}