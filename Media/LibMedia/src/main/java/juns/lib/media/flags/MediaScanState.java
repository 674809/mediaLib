package juns.lib.media.flags;

/**
 * Media scan state.
 *
 * @author Jun.Wang
 */
public final class MediaScanState {
    /**
     * 开始
     */
    public static final int START = 1;

    /**
     * 扫描媒体结束
     * <p>指扫描线程结束了,但是解析逻辑仍然有可能在持续中</p>
     */
    public static final int SCANNING_END = 2;

    /**
     * 扫描音频结束
     * <p>解析音频线程结束了</p>
     */
    public static final int SCAN_AUDIO_END = 3;

    /**
     * 扫描视频结束
     * <p>解析视频线程结束了</p>
     */
    public static final int SCAN_VIDEO_END = 4;

    /**
     * 扫描图片结束
     * <p>解析图片线程结束了</p>
     */
    public static final int SCAN_IMAGE_END = 5;

    /**
     * 扫描整体逻辑结束
     * <p>扫描线程结束了，且解析[音频/视频/图片]线程都结束了</p>
     */
    public static final int END = 6;

    /**
     * Get state description.
     */
    public static String desc(int scanState) {
        switch (scanState) {
            case START:
                return "START";
            case SCANNING_END:
                return "SCANNING_END";
            case SCAN_AUDIO_END:
                return "SCAN_AUDIO_END";
            case SCAN_VIDEO_END:
                return "SCAN_VIDEO_END";
            case SCAN_IMAGE_END:
                return "SCAN_IMAGE_END";
            case END:
                return "END";
            default:
                return "";
        }
    }
}
