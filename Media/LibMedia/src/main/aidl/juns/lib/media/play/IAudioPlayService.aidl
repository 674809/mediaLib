// IPlayDelegate.aidl
package juns.lib.media.play;

// Declare any non-default types here with import statements
import juns.lib.media.play.IAudioPlayListener;
import juns.lib.media.bean.ProAudio;

/**
 * AudioPlayService proxy class.
 *
 * @author Jun.Wang
 */
interface IAudioPlayService {
    /**
     * Player - Add {@link juns.lib.media.play.IAudioPlayListener}
     *
     * @param isRespDelta true-Will reponse the delta data during scanning.
     * @param tag Listener tag, sugguest set application package.
     * @param l Listener object, must be Stub object.
     */
    void addPlayListener(boolean isRespDelta,String tag,in IAudioPlayListener l);

    /**
     * Player - Remove{@link juns.lib.media.play.IAudioPlayListener}
     * @param tag Listener tag, sugguest set application package.
     * @param l Listener object, must be Stub object.
     */
    void removePlayListener(String tag,in IAudioPlayListener l);

    /**
     * Automatically play after media list is loaded.
     * <p>Usually use at first time when page opened.<p>
     */
    void autoPlay();

    /**
     * MediaScanService - Scanning state check.
     *
     * @return Scanning state. Boolean type.
     */
    boolean isScanning();

    /**
     * Get media count in database.
     *
     * @return Media count in database.
     */
    long getCountInDB();

    /**
     * Get mounted storage list.
     *
     * @return Mounted storage list.
     */
    List getStorageDevices();

    /**
     * Update collect state.
     *
     * @param position  Collected position of list.
     * @param media  Media to collect.
     * @return Integer type, Over 0 means successfully.
     */
     int updateMediaCollect(int position, in ProAudio media);

    /**
     * Delete history collect record.
     * <p>When you insert new storage, once execute collect, you should clear all the collect record of the history storages.<p>
     *
     * @return Integer type, Over 0 means successfully.
     */
    int clearHistoryCollect();

    /**
     * Get current position in play list
     */
    int getCurrPos();
    /**
     * Get total count of play list
     */
    int getTotalCount();
    /**
     * Get current media who is playing or ready to play.
     */
    ProAudio getCurrMedia();
    /**
     * Get current media path who is playing or ready to play.
     * @return String type, such as "/storage/emulated/0/111.flac".
     */
    String getCurrMediaPath();
    /**
     * Get current progress.
     *
     * @return Long type, unit is millisecond.
     */
    long getProgress();
    /**
     * Get current duration.
     *
     * @return Long type, unit is millisecond.
     */
    long getDuration();
    /**
     * Check playing state of player.
     */
    boolean isPlaying();

    /**
     * Update play list.
     * @param params <p>null means will query all data.</p>
     *               <p>Audios : [0]folderName,[1]mediaName,[2]fileName, [3]artistName,[4]albumName,[5]collected</p>
     *               <p>Videos or Images: [0]folderName,[1]mediaName,[2]fileName</p>
     */
    void applyPlayList(in String []params);
    /**
     * Update play information.
     * @param mediaUrl - Media path to play.
     * @param pos - Media position to play.
     */
    void applyPlayInfo(String mediaUrl,int pos);


    /**
     * Execute play.
     */
    void play();
    /**
     * User action - play selected media.
     *
     * @param mediaPath "../sdcard/Music/test.mp3"
     */
    void playByUrlByUser(String mediaPath);

   /**
    * User action - play previous.
    */
    void playPrevByUser();
   /**
    * User action - play next.
    */
    void playNextByUser();
   /**
    * User action - play or pause.
    */
    void playOrPauseByUser();

    /**
     * Execute release player.
     */
    void release();

    /**
     * Seek to fixed position.
     *
     * @param time Long type, unit is millisecond.
     */
    void seekTo(int time);
    /**
     * Check seek state.
     * <p>
     * When debugging, it is found that if a video encoding is abnormal in a certain section,
     * then dragging it to this section will cause system downtime.
     * In order to avoid this,
     * a marker is needed to detect whether the Seek action is being performed at this time.
     * </P>
     */
    boolean isSeeking();

    /**
     * Get history media url.
     * <p>Used to restore playback.</p>
     */
    String getLastMediaUrl();
    /**
     * Get history progress
     * <p>Used to restore playback.</p>
     */
    long getLastProgress();

    /**
     * Switch play mode.
     * <p>LOOP ->RANDOM ->SINGLE RANDOM ->ORDER</p>
     *
     * @param supportFlag {@link juns.lib.media.flags.PlayModeSupportType}
     */
    void switchPlayMode(int supportFlag);
    /**
     * Set play mode.
     *
     * @param mode {@link juns.lib.media.flags.PlayMode}
     */
    void setPlayMode(int mode);
    /**
     * Get play mode
     *
     * @return {@link juns.lib.media.flags.PlayMode}
     */
    int getPlayMode();

    /**
     * Make player focus.
     * <p>Means media player is at foreground.</p>
     */
    void focusPlayer();
    /**
     * AudioFocus - Check if media player is focused.
     *
     * @return true means [mCurrRegFlag == AudioManager.AUDIOFOCUS_GAIN]
     */
    boolean isPlayerFocused();
}
