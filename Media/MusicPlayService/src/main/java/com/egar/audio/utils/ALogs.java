package com.egar.audio.utils;

import android.content.Context;

import juns.lib.android.utils.Logs;

public class ALogs extends Logs {
    public static void sInit(Context context){
        Logs.init(context);
        Logs.setLogPrefix("AUDIO_");
    }
}
