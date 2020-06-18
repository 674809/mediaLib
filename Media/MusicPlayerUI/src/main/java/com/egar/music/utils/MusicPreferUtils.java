package com.egar.music.utils;

import juns.lib.android.utils.SpUtils;

public class MusicPreferUtils {
    /**
     * Used to flag warning information
     * <p>
     * <p>0 "测试版本" - 不提示无U盘</p>
     * <p>1 "正式版本" - 提示无U盘</p>
     */
    public static int getNoUDiskToastFlag(boolean isSet) {
        final String preferKey = "AUDIO_PLAYER_NO_UDISK_TOAST_FLAG";
        int flag = SpUtils.instance().getInt(preferKey, 0);
        if (isSet) {
            switch (flag) {
                case 1:
                    flag = 0;
                    break;
                case 0:
                    flag = 1;
                    break;
            }
            SpUtils.instance().saveInt(preferKey, flag);
        }
        return flag;
    }
}
