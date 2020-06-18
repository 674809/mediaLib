package xskin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Common PreferenceHelper
 * <p>
 * 必须初始化 {@link #init(Context)}
 * </p>
 *
 * @author Jun.Wang
 */
public class SpHelper {

    /**
     * SharedPreferences Object
     */
    private static SharedPreferences sPreferences;

    /**
     * Initialize On Application Start
     */
    public static void init(Context context) {
        Context appCxt = context.getApplicationContext();
        if (sPreferences == null) {
            sPreferences = PreferenceManager.getDefaultSharedPreferences(appCxt);
        }
    }

    /**
     * Save Integer
     */
    public static void saveInt(String key, Integer value) {
        sPreferences.edit().putInt(key, value).apply();
    }

    /**
     * Get Integer
     */
    public static Integer getInt(String key, Integer defaultValue) {
        return sPreferences.getInt(key, defaultValue);
    }
}
