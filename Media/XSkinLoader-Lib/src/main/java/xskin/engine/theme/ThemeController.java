package xskin.engine.theme;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.wind.me.xskinloader.util.AssetFileUtils;

import java.io.File;

import xskin.utils.SpHelper;

/**
 * Theme controller
 * <p>Used to listener </p>
 *
 * @author Jun.Wang
 */
public class ThemeController implements ThemeActions {
    //TAG
    private static final String TAG = "ThemeController";

    /**
     * {@link Context}
     */
    private Activity mContext;

    /**
     * {@link SkinFileInfo} set, used to save skin package information.
     */
    private SparseArray<SkinFileInfo> mSaSkinFileInfos = new SparseArray<>();

    /**
     * Theme Uri
     */
    private Uri mThemeUri;
    // Default theme Uri path
    public static final String DEFAULT_THEME_URI = "content://carsettings/carinfo/theme_setting";
    /**
     * Inner class {@link ThemeChangeContentObserver} object.
     */
    private ContentObserver mThemeObserver;

    /**
     * Inner interface {@link ThemeControllerListener} object
     */
    private ThemeControllerListener mThemeChangeListener;

    /**
     * Used to listener theme change state.
     */
    public interface ThemeControllerListener {
        /**
         * Get current theme value.
         *
         * @param isCallByController true means this value is called by {@link ThemeController}.
         *                           <p>In fact , you don't need care it for useless.</p>
         * @return the value of theme flag.
         */
        int getThemeValue(boolean isCallByController);

        /**
         * @param oldThemeType  last theme flag value.
         * @param currThemeType current theme flag value.
         */
        void onThemeChanged(int oldThemeType, int currThemeType);
    }

    /**
     * Constructor
     *
     * @param context Must be activity
     * @param handler Must be handler from Activity
     * @param uri     Theme flag is saved in ContentProvider, So uri is used to listener flag change state.
     *                This value could be null, default value is {@link #DEFAULT_THEME_URI}
     */
    public ThemeController(@NonNull Activity context, @NonNull Handler handler, @Nullable Uri uri) {
        mContext = context;
        if (uri == null) {
            mThemeUri = Uri.parse(DEFAULT_THEME_URI);
        } else {
            mThemeUri = uri;
        }
        Log.i(TAG, "mThemeUri: [" + mThemeUri.getPath() + "]");
        registerUiObserver(handler);
    }

    /**
     * Register {@link ContentObserver} to listener theme flag.
     */
    private void registerUiObserver(Handler handler) {
        try {
            mThemeObserver = new ThemeChangeContentObserver(handler);
            mContext.getContentResolver().registerContentObserver(mThemeUri, true, mThemeObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * INNER class - Theme observer class.
     */
    private class ThemeChangeContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        ThemeChangeContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.i(TAG, "onChange(" + selfChange + ")");
//            if (selfChange) {
            checkAndUpdateTheme();
//            }
        }
    }

    private void checkAndUpdateTheme() {
        if (mThemeChangeListener != null) {
            int currThemeFlag = getSkinFlag(false, 0);
            int latestThemeFlag = mThemeChangeListener.getThemeValue(true);
            Log.i(TAG, "checkAndUpdateTheme() >> {currThemeFlag:" + currThemeFlag + " , latestThemeFlag:" + latestThemeFlag + "}");
            if (currThemeFlag != latestThemeFlag) {
                getSkinFlag(true, latestThemeFlag);
                mThemeChangeListener.onThemeChanged(currThemeFlag, latestThemeFlag);
            }
        }
    }

    /**
     * Save theme flag to {@link android.content.SharedPreferences}
     * or get theme flag from {@link android.content.SharedPreferences}
     *
     * @param isSet Set or not.
     * @param val   The theme flag to set.
     * @return The theme saved flag.
     */
    private static int getSkinFlag(boolean isSet, int val) {
        final String PREFER_KEY = "X_SKIN_LOADER_SKIN_FLAG";
        if (isSet) {
            SpHelper.saveInt(PREFER_KEY, val);
        }
        return SpHelper.getInt(PREFER_KEY, -1);
    }

    /**
     * Add callback listener
     *
     * @param l {@link ThemeControllerListener}
     */
    public void addCallback(ThemeControllerListener l) {
        mThemeChangeListener = l;
    }

    /**
     * {@link Activity#onResume()} action
     * Activity onResume action.
     */
    public void onResume() {
        Log.i(TAG, "onResume()");
        checkAndUpdateTheme();
    }

    /**
     * {@link Activity#finish()} action.
     */
    public void finish() {
        Log.i(TAG, "finish()");
        destroy();
    }

    /**
     * {@link Activity#onDestroy()} action.
     */
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        destroy();
    }

    private void destroy() {
        try {
            mThemeChangeListener = null;
            if (mContext != null) {
                mContext.getContentResolver().unregisterContentObserver(mThemeObserver);
                mThemeObserver = null;
                mContext = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSkinFileInfo(int themeFlag, @Nullable String themeDesc, @NonNull String assetDir) {
        if (themeFlag < 0
                || TextUtils.isEmpty(assetDir) || TextUtils.isEmpty(assetDir.trim())) {
            return;
        }

        try {
            SkinFileInfo sfi = new SkinFileInfo();
            sfi.setThemeFlag(themeFlag);
            sfi.setSaveDir(mContext.getCacheDir().getAbsolutePath() + "/skins");
            sfi.setAssetDir(assetDir);
            if (themeDesc == null) {
                sfi.setSaveFileName(themeFlag + ".skin");
            } else {
                sfi.setSaveFileName(themeFlag + "_" + themeDesc + ".skin");
            }
            mSaSkinFileInfos.put(themeFlag, sfi);
        } catch (Exception e) {
            Log.i(TAG, "addSkinFileInfo() >> e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String releaseSkinFile(int themeFlag) {
        Log.i(TAG, "releaseSkinFile(" + themeFlag + ")");
        SkinFileInfo sfi = mSaSkinFileInfos.get(themeFlag);
        if (sfi != null) {
            String assetSkinPath = sfi.getAssetDir();
            File localSkinFile = new File(sfi.getSaveDir() + File.separator + sfi.getSaveFileName());
            if (localSkinFile.exists()) {
                //Compare md5
                if (AssetFileUtils.isSameFile(mContext, assetSkinPath, localSkinFile.getPath())) {
                    Log.i(TAG, "releaseSkinFile() - Exist same skin file.");
                    return localSkinFile.getPath();
                }

                // Delete un-useful skin file.
                if (localSkinFile.exists()) {
                    localSkinFile.delete();
                    Log.i(TAG, "releaseSkinFile() - Delete existed rubbish skin file.");
                }
            }

            //Copy skin file to local.
            boolean execSuccessfully = AssetFileUtils.copyAssetFile(mContext.getApplicationContext(),
                    sfi.getAssetDir(), sfi.getSaveDir(), sfi.getSaveFileName());
            Log.i(TAG, "releaseSkinFile() - [execSuccessfully:" + execSuccessfully + "]");
            if (execSuccessfully) {
                return localSkinFile.getPath();
            }
        }

        Log.i(TAG, "releaseSkinFile() - Skin file is not exist.");
        return null;
    }
}
