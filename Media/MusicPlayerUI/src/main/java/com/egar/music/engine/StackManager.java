package com.egar.music.engine;

import android.app.Activity;

import java.util.Stack;

import juns.lib.android.utils.Logs;

/**
 * Activity stack manger.
 *
 * @author Jun.Wang
 */
public class StackManager implements StackManagerDelegate {
    //TAG
    private static final String TAG = "StackManager";

    //Activity stack
    private Stack<Activity> mActivityStack;

    public StackManager() {
    }

    @Override
    public void add(Activity activity) {
        Logs.i(TAG, "add(" + activity + ")");
        if (activity == null) {
            return;
        }
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(activity);
    }

    @Override
    public void remove(Activity activity) {
        Logs.i(TAG, "remove(" + activity + ")");
        if (activity == null || mActivityStack == null || mActivityStack.size() == 0) {
            return;
        }
        mActivityStack.remove(activity);
    }

    @Override
    public void exitApp() {
        Logs.i(TAG, "exitApp()");
        while (true) {
            Activity activity = current();
            if (activity == null) {
                break;
            } else {
                activity.finish();
                remove(activity);
            }
        }
    }

    @Override
    public Activity current() {
        if (mActivityStack == null || mActivityStack.size() == 0) {
            return null;
        }
        return mActivityStack.lastElement();
    }
}
