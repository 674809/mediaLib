package xskin.utils;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.wind.me.xskinloader.SkinInflaterFactory;
import com.wind.me.xskinloader.SkinManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkinUtil {

    /**
     * The view u add by dynamically
     */
    private Map<String, Set<View>> mMapCachedViews = new HashMap<>();

    private SkinUtil() {
    }

    private static class SingletonHolder {
        private static final SkinUtil INSTANCE = new SkinUtil();
    }

    public static SkinUtil instance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialize in application.
     */
    public void init(Application application) {
        SkinManager.get().init(application);
        // If u often use application context to initialize widget,
        // u should set application LayoutInflater to  SkinInflaterFactory
        SkinInflaterFactory.setFactory(LayoutInflater.from(application));
    }

    /**
     * Initialize in activity.
     */
    public void init(Activity activity) {
        SkinManager.get().init(activity);
        SkinInflaterFactory.setFactory(activity);
    }

    public void setTextViewColor(Activity activity, View view, int resId) {
        SkinManager.get().setTextViewColor(view, resId);
        save(activity, view);
    }

    public void setHintTextColor(Activity activity, View view, int resId) {
        SkinManager.get().setHintTextColor(view, resId);
        save(activity, view);
    }

    public void setViewBackground(Activity activity, View view, int resId) {
        SkinManager.get().removeObservableView(view);
        SkinManager.get().setViewBackground(view, resId);
        save(activity, view);
    }

    public void setImageDrawable(Activity activity, View view, @DrawableRes int resId) {
        SkinManager.get().setImageDrawable(view, resId);
        save(activity, view);
    }

    public void setListViewSelector(Activity activity, View view, int resId) {
        SkinManager.get().setListViewSelector(view, resId);
        save(activity, view);
    }

    public void setListViewDivider(Activity activity, View view, int resId) {
        SkinManager.get().setListViewDivider(view, resId);
        save(activity, view);
    }

    public void setProgressBarIndeterminateDrawable(Activity activity, View view, int resId) {
        SkinManager.get().setProgressBarIndeterminateDrawable(view, resId);
        save(activity, view);
    }

    public void setWindowStatusBarColor(Window window, @ColorRes int resId) {
        SkinManager.get().setWindowStatusBarColor(window, resId);
    }

    private void save(Activity activity, View view) {
        try {
            String actClsName = activity.getClass().getName();
            Set<View> cachedViews = mMapCachedViews.get(actClsName);
            if (cachedViews == null) {
                cachedViews = new HashSet<>();
            }
            cachedViews.add(view);
            mMapCachedViews.put(actClsName, cachedViews);
        } catch (Exception e) {
            Log.i("", "");
        }
    }

    public synchronized void clear(Activity activity) {
        try {
            SkinManager instance = SkinManager.get();
            Set<View> cachedViews = mMapCachedViews.get(activity.getClass().getName());
            if (cachedViews != null) {
                for (View cachedView : cachedViews) {
                    instance.removeObservableView(cachedView);
                }
            }
        } catch (Exception e) {
            Log.i("", "");
        }
    }

    /**
     * 加载新皮肤
     *
     * @param skinApkPath 新皮肤路径
     * @return true 加载新皮肤成功 false 加载失败
     */
    public boolean loadNewSkin(String skinApkPath) {
        return SkinManager.get().loadNewSkin(skinApkPath);
    }

    /**
     * 回滚默认皮肤
     */
    public void restoreDefaultSkin() {
        SkinManager.get().restoreToDefaultSkin();
    }

    public Drawable getDrawable(@DrawableRes int resId) {
        return SkinManager.get().getDrawable(resId);
    }

    public int getColor(@ColorRes int resId) {
        return SkinManager.get().getColor(resId);
    }
}
