package com.egar.music.engine;

import android.app.Activity;

/**
 * Activity stack manger actions.
 *
 * @author Jun.Wang
 */
public interface StackManagerDelegate {
    /**
     * Add activity to stack.
     */
    void add(Activity activity);

    /**
     * Remove activity from stack.
     */
    void remove(Activity activity);

    /**
     * Exit application, means clear all activities.
     */
    void exitApp();

    /**
     * Get current activity.
     */
    Activity current();
}
