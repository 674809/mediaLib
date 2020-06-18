package com.egar.scanner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.egar.scanner.service.ScannerService;

import juns.lib.android.utils.Logs;
import juns.lib.media.action.MediaActions;

/**
 * Media scanner receiver
 *
 * @author Jun.Wang
 */
public class MediaScanReceiver extends BroadcastReceiver {
    //TAG
    private static final String TAG = "MediaScanReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logs.i(TAG, "action : " + action);
        //Let service process logic
        if (action != null) {
            switch (action) {
                case MediaActions.BOOT_COMPLETED:
                    Logs.switchEnable(true);
                    break;
                case MediaActions.OPEN_SCAN_LOGS:
                    Logs.switchEnable(false);
                    break;
                default:
                    startMediaScanService(context, action);
                    break;
            }
        }
    }

    /**
     * Start scanner service
     *
     * @param context {@link Context}
     * @param action  see{@link juns.lib.media.action.MediaActions}
     */
    public void startMediaScanService(Context context, String action) {
        Intent intentScan = new Intent(context, ScannerService.class);
        intentScan.putExtra(ScannerService.PARAM_ACTION, action);
        context.startService(intentScan);
    }
}
