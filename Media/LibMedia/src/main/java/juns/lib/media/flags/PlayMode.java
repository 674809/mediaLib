package juns.lib.media.flags;

/**
 * 播放模式
 * <p>
 * 音乐播放器播放模式，如"单曲循环/随机模式/循环模式/顺序模式"
 *
 * @author Jun.Wang
 */
public final class PlayMode {
    /**
     * 单曲循环
     */
    public static final int SINGLE = 1;
    /**
     * 随机模式
     */
    public static final int RANDOM = 2;
    /**
     * 循环模式
     */
    public static final int LOOP = 3;
    /**
     * 顺序模式
     */
    public static final int ORDER = 4;

    /**
     * Get description.
     */
    public static String desc(int type) {
        switch (type) {
            case SINGLE:
                return "SINGLE";
            case RANDOM:
                return "RANDOM";
            case LOOP:
                return "LOOP";
            case ORDER:
                return "ORDER";
            default:
                return "";
        }
    }
}
