package juns.lib.media.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import juns.lib.java.utils.JsFileUtils;
import juns.lib.media.flags.MediaType;

/**
 * Scanner file util methods.
 *
 * @author Jun.Wang
 */
public class ScannerFileUtils extends JsFileUtils {
    //TAG
    private static final String TAG = "ScannerFileUtils";

    /**
     * APP 文件存储根路径
     * <p>"/storage/emulated/0/Android/data/com.egar.scanner/files/db"</p>
     * <p>"/data/user/0/com.egar.scanner/files/db"</p>
     */
    private static String mAppCachePath = "";

    /**
     * 创建 APP 文件存储根路径
     */
    public static void init(Context cxt) {
        try {
            File appCacheFile;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                appCacheFile = cxt.getApplicationContext().getExternalFilesDir(null);
            } else {
                appCacheFile = cxt.getApplicationContext().getFilesDir();
            }

            if (appCacheFile != null) {
                mAppCachePath = appCacheFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // LOG
        Log.i(TAG, "mAppCachePath : " + mAppCachePath);
    }

    /**
     * 获取文件存储根路径
     *
     * @param type {@link MediaType}
     * @return String Database file absolute path.
     */
    public static String getDbFilePath(int type) {
        String dbPath;
        switch (type) {
            case MediaType.IMAGE:
                dbPath = mAppCachePath + "/db/Images.sqlite";
                break;
            case MediaType.VIDEO:
                dbPath = mAppCachePath + "/db/Videos.sqlite";
                break;
            case MediaType.AUDIO:
            default:
                dbPath = mAppCachePath + "/db/Audios.sqlite";
                break;
        }
        return dbPath;
    }

    /**
     * 获取媒体封面路径
     * <p>"/sdcard/music/123.mp3" 一般会转换为 ""</p>
     *
     * @param type        {@link MediaType}
     * @param storePath   媒体文件，如"/sdcard/music/123.mp3"
     * @param titlePinYin 媒体文件名称拼音。
     * @return 封面图片路径，如"通用存储路径/sdcard_music_123.png"
     */
    public static String getCoverImgPath(int type, String storePath, String titlePinYin) {
        StringBuilder coverImgPath = new StringBuilder();
        try {
            switch (type) {
                case MediaType.VIDEO:
                    coverImgPath.append(mAppCachePath)
                            .append(File.separator)
                            .append(".cover_img_video");
                    break;
                case MediaType.AUDIO:
                default:
                    coverImgPath.append(mAppCachePath)
                            .append(File.separator)
                            .append(".cover_img_audio");
                    break;
            }
            createFolder(coverImgPath.toString());

            //
            coverImgPath.append(File.separator)
                    .append(storePath.replace("/", "_"))
                    .append("_")
                    .append(titlePinYin).append(".png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coverImgPath.toString();
    }
}
