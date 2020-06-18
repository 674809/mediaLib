package juns.lib.media.flags;

/**
 * 播放模式支持标记
 * <p>目前常用的播放模式里面无顺序模式</p>
 *
 * @author Jun.Wang
 */
public final class PlayModeSupportType {
    /**
     * 支持 LOOP/RANDOM/SINGLE/ORDER
     */
    public static final int ALL = 1;

    /**
     * 支持 LOOP/RANDOM/SINGLE
     */
    public static final int NO_ORDER = 2;

    public static String desc(int type) {
        switch (type) {
            case ALL:
                return "ALL";
            case NO_ORDER:
                return "NO_ORDER";
            default:
                return "";
        }
    }
}
