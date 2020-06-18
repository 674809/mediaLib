package juns.lib.media.action;

import android.os.RemoteException;

/**
 * Play progress Listener
 *
 * @author Jun.Wang
 */
public interface IPlayProgressListener {
    /**
     * Progress change callback
     */
    void onProgressChanged(String mediaPath, int progress, int duration) throws RemoteException;
}
