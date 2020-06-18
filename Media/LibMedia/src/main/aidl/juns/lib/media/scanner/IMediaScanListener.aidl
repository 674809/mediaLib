// IMediaScanListener.aidl
package juns.lib.media.scanner;

// Declare any non-default types here with import statements
import java.util.List;

/**
 * Reponse listener of MediaScanService.
 *
 * @author Jun.Wang
 */
interface IMediaScanListener {
   /**
    * Media scanning state response.
    * @param state {@link juns.lib.media.engine.MediaScanState}
    *              <p>1 START</p>
    *              <p>2 REFRESHING</p>
    *              <p>3 END</p>
    */
    void onRespScanState(int state);

    /**
     * Report [List of devices whose mount status has changed.]
     */
    void onRespMountChange(out List listStorageDevices);

    /**
     * MediaScanService
     * @param listMedias -Delta list during scanning.
     */
    void onRespDeltaMedias(in List listMedias);
}