package com.egar.music.utils;

import android.content.Context;

import juns.lib.android.utils.Logs;

public class MLogs extends Logs {
    public static void sInit(Context context) {
        Logs.init(context);
        Logs.setLogPrefix("MUSIC_");
    }
}
