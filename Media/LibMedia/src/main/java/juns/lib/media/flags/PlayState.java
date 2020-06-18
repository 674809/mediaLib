package juns.lib.media.flags;

/**
 * 播放器响应状态定义
 *
 * @author Jun.Wang
 */
public final class PlayState {
    /**
     * 状态初始化
     */
    public static final int NONE = 0;

    /**
     * 表示执行了播放动作，但是并不代表已经开始了播放
     */
    public static final int PLAY = 1;

    /**
     * 表示媒体播放器已经做好了播放准备
     */
    public static final int PREPARED = 2;

    /**
     * 播放暂停
     */
    public static final int PAUSE = 3;

    /**
     * 当前媒体播播放结束
     * <p>可以据此判断是否要执行下一个媒体的播放</p>
     */
    public static final int COMPLETE = 4;

    /**
     * 播放器资源释放了
     */
    public static final int RELEASE = 5;

    /**
     * 媒体播放器进度跳转可能是异步的，需要在跳转结束后回调通知结束。
     */
    public static final int SEEK_COMPLETED = 6;

    /**
     * 播放器产生了异常 - 通用异常标记
     */
    public static final int ERROR = 100;

    /**
     * 播放器产生了异常 - 播放器初始化失败
     */
    public static final int ERROR_PLAYER_INIT = 101;

    /**
     * ERROR : File is not exist.
     */
    public static final int ERROR_FILE_NOT_EXIST = 102;

    /**
     * Notify Refresh UI, EXEC before Prepare() or PrepareSync();
     */
    public static final int REFRESH_UI = 200;

    /**
     * Get description.
     */
    public static String desc(int type) {
        switch (type) {
            case NONE:
                return "NONE";
            case PLAY:
                return "PLAY";
            case PREPARED:
                return "PREPARED";
            case PAUSE:
                return "PAUSE";
            case COMPLETE:
                return "COMPLETE";
            case RELEASE:
                return "RELEASE";
            case SEEK_COMPLETED:
                return "SEEK_COMPLETED";
            case ERROR:
                return "ERROR";
            case ERROR_PLAYER_INIT:
                return "ERROR_PLAYER_INIT";
            case ERROR_FILE_NOT_EXIST:
                return "ERROR_FILE_NOT_EXIST";
            case REFRESH_UI:
                return "REFRESH_UI";
            default:
                return "";
        }
    }
}
