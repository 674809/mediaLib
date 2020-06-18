package xskin.widget.base;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import xskin.engine.theme.ThemeActions;
import xskin.engine.theme.ThemeController;
import xskin.engine.theme.ThemeController.ThemeControllerListener;
import xskin.utils.SkinUtil;
import xskin.utils.SpHelper;

/**
 * Base {@link FragmentActivity}
 * <p>1. Listener theme change. You must override {@link #getThemeValue(boolean)} and {@link #onThemeChanged(int, int)}</p>
 */
public abstract class BaseFragActivity extends FragmentActivity implements ThemeActions, ThemeControllerListener {
    //TAG
    private static final String TAG = "ThemeBaseActivity";

    /**
     * Used to listener theme change.
     */
    private ThemeController mThemeController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        //Initialize skin controller
        SpHelper.init(this);
        SkinUtil.instance().init(this);
        //Initialize ThemeController
        initThemeController(null);
    }

    /**
     * Initialize ThemeController
     * <p>This method could be executed by yourself or not.</p>
     *
     * @param uri Theme flag is saved in ContentProvider, So uri is used to listener flag change state.
     *            This value could be null, default value is {@link ThemeController#DEFAULT_THEME_URI}
     */
    protected void initThemeController(Uri uri) {
        if (mThemeController != null) {
            mThemeController.finish();
        }
        mThemeController = new ThemeController(this, new Handler(), uri);
        mThemeController.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mThemeController != null) {
            mThemeController.onResume();
        }
    }

    @Override
    public int getThemeValue(boolean isCallByController) {
        return 0;
    }

    @Override
    public void onThemeChanged(int oldThemeType, int currThemeType) {
        Log.i(TAG, "onThemeChanged(" + oldThemeType + "," + currThemeType + ")");
        String skinFilePath = releaseSkinFile(currThemeType);
        if (TextUtils.isEmpty(skinFilePath)) {
            SkinUtil.instance().restoreDefaultSkin();
        } else {
            SkinUtil.instance().loadNewSkin(skinFilePath);
        }
    }

    @Override
    public void addSkinFileInfo(int themeFlag, @Nullable String themeDesc, @NonNull String assetDir) {
        if (mThemeController != null) {
            mThemeController.addSkinFileInfo(themeFlag, themeDesc, assetDir);
        }
    }

    @Override
    public String releaseSkinFile(int themeFlag) {
        if (mThemeController != null) {
            return mThemeController.releaseSkinFile(themeFlag);
        }
        return null;
    }

    @Override
    public void finish() {
        if (mThemeController != null) {
            mThemeController.finish();
        }
        clearActivity();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (mThemeController != null) {
            mThemeController.onDestroy();
        }
        clearActivity();
        super.onDestroy();
    }

    private void clearActivity() {
        SkinUtil.instance().clear(this);
    }
}