package juns.lib.media.player;

import android.media.MediaPlayer;

import juns.lib.android.utils.Logs;

public class MediaUtils {
    // TAG
    private static final String TAG = "MediaPlayerUtils";

    /**
     * Get Media Suffix
     */
    public static String getSuffix(String fPathOrDisPlayName) {
        try {
            return fPathOrDisPlayName.substring(fPathOrDisPlayName.lastIndexOf("."));
        } catch (Exception e) {
            Logs.debugI(TAG, "getSuffix(" + fPathOrDisPlayName + ") >> e: " + e.getMessage());
        }
        return "";
    }

    /**
     * Print MeidaPlayer Error
     */
    public static void printError(MediaPlayer mp, int what, int extra) {
        Logs.i(TAG, "printError(mp,what,extra) -> [what:" + what + " ; extra:" + extra + "]");
        // what
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Logs.i(TAG, "发生未知错误");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Logs.i(TAG, "媒体服务器死机");
                break;
            default:
                Logs.i(TAG, "Default what Error");
                break;
        }

        // extra
        switch (extra) {
            // I/O 读写错误
            case MediaPlayer.MEDIA_ERROR_IO:
                Logs.i(TAG, "文件或网络相关的IO操作错误");
                break;

            // 文件格式不支持
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                Logs.i(TAG, "比特流编码标准或文件不符合相关规范");
                break;

            // 一些操作需要太长时间来完成,通常超过3 - 5秒。
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Logs.i(TAG, "操作超时");
                break;

            // 比特流编码标准或文件符合相关规范,但媒体框架不支持该功能
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Logs.i(TAG, "比特流编码标准或文件符合相关规范,但媒体框架不支持该功能");
                break;
            default:
                Logs.i(TAG, "Default extra Error");
                break;
        }
    }
}
