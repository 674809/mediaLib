package com.egar.scanner.utils;

import android.content.Context;

import juns.lib.android.utils.Logs;

public class SLogs extends Logs {
    public static void sInit(Context context){
        Logs.init(context);
        Logs.setLogPrefix("SCAN_");
    }
}
