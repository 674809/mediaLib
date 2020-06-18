package xskin.widget.base;

import android.app.Application;

import xskin.utils.SkinUtil;
import xskin.utils.SpHelper;

/**
 * Base {@link Application}
 * <p>1. Listener theme change}</p>
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        SpHelper.init(this);
        SkinUtil.instance().init(this);
    }
}
