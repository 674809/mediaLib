package juns.lib.android.utils;

import android.content.Context;
import android.util.Log;

/**
 * Log tool class
 * <p>First , u must call {link {@link #init(Context)}}</p>
 *
 * @author Jun.Wang
 */
public class Logs {

    /**
     * Log print enable flag.
     */
    private static boolean mDebug = false;

    /**
     * 日志标签前缀
     */
    private static String mLogPrefix = "";

    /**
     * Initialize
     *
     * @param context {@link Context}
     */
    public static void init(Context context) {
        SpUtils.instance().init(context);
        mDebug = SpUtils.instance().isOpenLogs(false, false);
    }

    public static boolean isDebug() {
        return mDebug;
    }

    public static void setLogPrefix(String logPrefix) {
        mLogPrefix = logPrefix;
    }

    /**
     * Switch enable flag.
     *
     * @param isBootCompleted true: Restore flag after device booted ;
     *                        <p>false: Switch enable state.</p>
     */
    public static void switchEnable(boolean isBootCompleted) {
        Log.i("Logs", "switchEnable(" + isBootCompleted + ")");
        mDebug = SpUtils.instance().isOpenLogs(false, false);
        if (!isBootCompleted) {
            mDebug = !mDebug;
            SpUtils.instance().isOpenLogs(true, mDebug);
        }
    }

    /**
     * Parcel system method : {@link Log#i(String, String)}
     */
    public static void debugI(String tag, String msg) {
        if (mDebug) {
            i(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        Log.v(mLogPrefix + tag, msg);
    }

    /**
     * Parcel system method : {@link Log#d(String, String)}
     */
    public static void d(String tag, String msg) {
        Log.d(mLogPrefix + tag, msg);
    }

    /**
     * Parcel system method : {@link Log#i(String, String)}
     */
    public static void i(String tag, String msg) {
        Log.i(mLogPrefix + tag, msg);
    }

    /**
     * Parcel system method : {@link Log#w(String, String)}
     */
    public static void w(String tag, String msg) {
        Log.w(mLogPrefix + tag, msg);
    }

    /**
     * Parcel system method : {@link Log#e(String, String)}
     */
    public static void e(String tag, String msg) {
        Log.e(mLogPrefix + tag, msg);
    }
}
