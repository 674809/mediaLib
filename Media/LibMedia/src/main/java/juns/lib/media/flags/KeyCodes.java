package juns.lib.media.flags;

import android.view.KeyEvent;

/**
 * Customized keys
 *
 * @author Jun.Wang
 */
public final class KeyCodes {
    public static final int KEYCODE_VOLUME_UP = 24;
    public static final int KEYCODE_VOLUME_DOWN = 25;
    public static final int KEYCODE_VOLUME_MUTE = 164;
    public static final int KEYCODE_RADIO = 284;
    public static final int KEYCODE_DPAD_LEFT = 21;
    public static final int KEYCODE_DPAD_RIGHT = 22;
    public static final int KEYCODE_ENTER = 66;
    public static final int KEYCODE_HOME = 3;
    public static final int KEYCODE_BACK = 4;
    public static final int KEYCODE_MEDIA_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    public static final int KEYCODE_MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT;
    public static final int KEYCODE_MEDIA_PLAY_PAUSE = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
    public static final int KEYCODE_MEDIA_PLAY = KeyEvent.KEYCODE_MEDIA_PLAY;
    public static final int KEYCODE_MEDIA_PAUSE = KeyEvent.KEYCODE_MEDIA_PAUSE;

    /**
     * Get state description.
     */
    public static String desc(int type) {
        switch (type) {
            case KEYCODE_VOLUME_UP:
                return "KEYCODE_VOLUME_UP";
            case KEYCODE_VOLUME_DOWN:
                return "KEYCODE_VOLUME_DOWN";
            case KEYCODE_VOLUME_MUTE:
                return "KEYCODE_VOLUME_MUTE";
            case KEYCODE_RADIO:
                return "KEYCODE_RADIO";
            case KEYCODE_DPAD_LEFT:
                return "KEYCODE_DPAD_LEFT";
            case KEYCODE_DPAD_RIGHT:
                return "KEYCODE_DPAD_RIGHT";
            case KEYCODE_ENTER:
                return "KEYCODE_ENTER";
            case KEYCODE_HOME:
                return "KEYCODE_HOME";
            case KEYCODE_BACK:
                return "KEYCODE_BACK";
            case KEYCODE_MEDIA_PREVIOUS:
                return "KEYCODE_MEDIA_PREVIOUS";
            case KEYCODE_MEDIA_NEXT:
                return "KEYCODE_MEDIA_NEXT";
            case KEYCODE_MEDIA_PLAY_PAUSE:
                return "KEYCODE_MEDIA_PLAY_PAUSE";
            case KEYCODE_MEDIA_PLAY:
                return "KEYCODE_MEDIA_PLAY";
            case KEYCODE_MEDIA_PAUSE:
                return "KEYCODE_MEDIA_PAUSE";
            default:
                return "";
        }
    }
}