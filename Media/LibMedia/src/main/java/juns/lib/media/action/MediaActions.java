package juns.lib.media.action;

import android.content.Intent;

/**
 * Media broadcast actions.
 *
 * @author Jun.Wang
 */
public interface MediaActions {
    // >>>>-- Voice assistant --<<<<
    //---- Media common ----
    String MEDIA_PLAY = "com.egar.voice.adapter.MEDIA_PLAY";
    String MEDIA_PAUSE = "com.egar.voice.adapter.MEDIA_PAUSE";
    String MEDIA_PREV = "com.egar.voice.adapter.MEDIA_PREV";
    String MEDIA_NEXT = "com.egar.voice.adapter.MEDIA_NEXT";

    //---- Music ----
    String MUSIC_OPEN = "com.egar.voice.adapter.OPEN_MUSIC";
    String MUSIC_CLOSE = "com.egar.voice.adapter.CLOSE_MUSIC";

    //@param title 歌曲名
    //@param artist 歌手名
    //@path path 歌曲路径
    //@param type int类型
    //    2：随机播放一首歌曲。
    //    3：播放指定歌手的歌曲：
    //        必然包含artist参数；
    //        path如果为null，自行决定播放该歌手的哪首歌；
    //        path不为null，播放该路径歌曲。
    //    4：播放指定歌名的歌曲：
    //        必然含path参数，必须播放该首歌。
    //    5：播放指定歌手及歌名的歌曲：
    //        必然包含artist参数；
    //        path如果为null，自行决定播放该歌手的哪首歌；
    //        path不为null，播放该路径歌曲。
    String MUSIC_OPEN_PLAY = "com.egar.voice.adapter.OPEN_PLAY_MUSIC";

    //---- Video ----
    String VIDEO_OPEN = "com.egar.voice.adapter.OPEN_VIDEO";
    String VIDEO_CLOSE = "com.egar.voice.adapter.CLOSE_VIDEO";

    //---- Radio ----
    String RADIO_OPEN = "com.egar.voice.adapter.OPEN_RADIO";
    String RADIO_CLOSE = "com.egar.voice.adapter.CLOSE_RADIO";
    String RADIO_SET_FREQ = "com.egar.voice.adapter.SET_FREQ";

    /**
     * ### {@link juns.lib.android.utils.Logs} enable switch ###
     */
    String OPEN_SCAN_LOGS = "com.egar.scanner.test.OPEN_LOGS";//MediaScanService
    String OPEN_AUDIO_LOGS = "com.egar.audio.test.OPEN_LOGS";//AudioPlayService
    String OPEN_MUSIC_LOGS = "com.egar.music.test.OPEN_LOGS";//MusicPlayerUI

    /**
     * ### debug mode ###
     */
    String SWITCH_DEBUG_MODE = "com.egar.music.test.OPEN_MUSIC";//MusicPlayerUI

    // System Broadcast
    //Boot
    String BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;

    //Mount
    String STORAGE_MOUNTED = Intent.ACTION_MEDIA_MOUNTED;
    String STORAGE_UNMOUNTED = Intent.ACTION_MEDIA_UNMOUNTED;
    String STORAGE_EJECT = Intent.ACTION_MEDIA_EJECT;
    String STORAGE_TEST_MOUNTED = "com.egar.scanner.test.MOUNTED";
    String STORAGE_TEST_UN_MOUNTED = "com.egar.scanner.test.UN_MOUNTED";

    //Screen
    String SCREEN_ON = Intent.ACTION_SCREEN_ON;
    String SCREEN_OFF = Intent.ACTION_SCREEN_OFF;

    //MediaButton
    String MEDIA_BUTTON = Intent.ACTION_MEDIA_BUTTON;
}
