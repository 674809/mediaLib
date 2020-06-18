// IMediaScanService.aidl
package juns.lib.media.scanner;

import juns.lib.media.scanner.IMediaScanListener;
import java.util.List;

// Declare any non-default types here with import statements

/**
 * MediaScanService proxy class.
 *
 * @author Jun.Wang
 */
interface IMediaScanService {
    /**
     * Scanner - Add {@link juns.lib.media.scanner.IMediaScanListener}
     *
     * @param type {@link juns.lib.media.flags.MediaType}
     * @param isRespDelta true-Will reponse the delta data during scanning.
     * @param tag Listener tag, sugguest set application package.
     * @param l Listener object, must be Stub object.
     */
    void addScanListener(int type,boolean isRespDelta,String tag,in IMediaScanListener l);

    /**
     * Scanner - Remove{@link juns.lib.media.scanner.IMediaScanListener}
     * @param tag Listener tag, sugguest set application package.
     * @param l Listener object, must be Stub object.
     */
    void removeScanListener(String tag,in IMediaScanListener l);

    /**
    * Start scan media in mounted storage.
    * <p>Make MediaScanService scan.</p>
    */
    void startScan();

   /**
    * Scanning state check.
    *
    * @param type {@link juns.lib.media.flags.MediaType}
    *
    * @return Boolean type.Scanning state.
    */
    boolean isScanning(int type);

    /**
     * Update collect state.
     *
     * @param type {@link juns.lib.media.flags.MediaType}
     * @param position  Collected position of list.
     * @param media  Media to collect.
     * @return Integer type, Over 0 means successfully.
     */
     int updateMediaCollect(int type, in List mediasToCollect);

    /**
     * Delete history collect record.
     * <p>When you insert new storage, once execute collect, you should clear all the collect record of the history storages.<p>
     *
     * @param type {@link juns.lib.media.flags.MediaType}
     * @return Integer type, Over 0 means successfully.
     */
     int clearHistoryCollect(int type);

    /**
     * Get media count in database.
     *
     * @param type {@link juns.lib.media.flags.MediaType}
     * @return Media count in database.
     */
    long getCountInDB(int type);

    /**
     * Get mounted storage list.
     *
     * @return Mounted storage list.
     */
    List getStorageDevices();

    /**
     * Get database path.
     *
     * @param type {@link juns.lib.media.flags.MediaType}
     * @return Database path.
     */
    String getDbPath(int type);
}