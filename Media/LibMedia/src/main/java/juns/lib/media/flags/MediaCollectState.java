package juns.lib.media.flags;

/**
 * 媒体收藏状态
 *
 * @author Jun.Wang
 */
public final class MediaCollectState {
    public static final int COLLECTED = 1;
    public static final int UN_COLLECTED = 0;

    /**
     * Get description.
     */
    public static String desc(int type) {
        switch (type) {
            case COLLECTED:
                return "COLLECTED";
            case UN_COLLECTED:
                return "UN_COLLECTED";
            default:
                return "";
        }
    }
}
