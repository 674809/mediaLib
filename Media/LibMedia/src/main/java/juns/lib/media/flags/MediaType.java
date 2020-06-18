package juns.lib.media.flags;

/**
 * 媒体类型
 */
public final class MediaType {
    /**
     * 所有媒体类型
     */
    public static final int ALL = 0;

    /**
     * 音频
     */
    public static final int AUDIO = 1;

    /**
     * 视频
     */
    public static final int VIDEO = 2;

    /**
     * 图片
     */
    public static final int IMAGE = 3;

    /**
     * Get description.
     */
    public static String desc(int type) {
        switch (type) {
            case ALL:
                return "ALL";
            case AUDIO:
                return "AUDIO";
            case VIDEO:
                return "VIDEO";
            case IMAGE:
                return "IMAGE";
            default:
                return "";
        }
    }
}
