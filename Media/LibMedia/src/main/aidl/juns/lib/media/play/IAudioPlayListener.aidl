// IPlayListener.aidl
package juns.lib.media.play;

// Declare any non-default types here with import statements
import java.util.List;
import juns.lib.media.bean.StorageDevice;

/**
 * Reponse listener of AudioPlayService.
 *
 * @author Jun.Wang
 */
interface IAudioPlayListener {
    /**
     * Report [List of devices whose mount status has changed.]
     */
    void onMountStateChanged(out List listStorageDevices);

   /**
    * MediaScanService - SCANNING status.
    * @param state {@link juns.lib.media.flags.MediaScanState]
    */
    void onScanStateChanged(int state);

    /**
     * MediaScanService
     * <p>Delta list during scanning.</p>
     * @param listMedias -Delta list.
     */
    void onGotDeltaMedias(out List listMedias);

    /**
     * MusicPlayService
     * <p>Notify play state</p>
     * @param playState {@link juns.lib.media.flags.PlayState}
     */
    void onPlayStateChanged(int playState);

    /**
     * MusicPlayService
     * <p>Notify play progress</p>
     * @param progress -Current progress.
     * @param duration -Media total time.
     */
    void onPlayProgressChanged(String mediaPath, int progress, int duration);

    /**
     * MusicPlayService
     * <p>Notify play mode.</p>
     * @param newPlayMode -{@link juns.lib.media.flags.PlayMode}
     */
    void onPlayModeChanged(int newPlayMode);
}
