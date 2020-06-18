package juns.lib.media.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Utils;
import juns.lib.media.bean.StorageDevice;

/**
 * SDCard Common Methods
 *
 * @author Jun.Wang
 */
public class SDCardUtils {
    //TAG
    private static final String TAG = "SDCardUtils";

    public static String SDCARD_INTERNAL = "internal";
    public static String SDCARD_EXTERNAL = "external";
    public static String UDISK_EXTERNAL = "udisk";

    /**
     * Check SDCard Status
     */
    public static boolean isSDCardActive() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Get Inner storage.
     */
    public static File getInnerStorage() {
        return Environment.getExternalStorageDirectory();
    }

    /**
     * Get mounted storage map.
     *
     * @param cxt {@link Context}
     * @return HashMap{key[root path],value[StorageDevice]}
     */
    public static HashMap<String, StorageDevice> getMapMountedStorage(Context cxt) {
        HashMap<String, StorageDevice> resMapStorage = new HashMap<>();
        HashMap<String, StorageDevice> tmpMapStorage = getSDCardInfos(cxt);
        if (tmpMapStorage != null) {
            for (StorageDevice storage : tmpMapStorage.values()) {
                if (storage.isMounted()) {
                    resMapStorage.put(storage.getRoot(), storage);
                }
            }
        }
        return resMapStorage;
    }

    /**
     * Get mounted usb map.
     *
     * @param cxt {@link Context}
     * @return HashMap{key[root path],value[StorageDevice]}
     */
    public static HashMap<String, StorageDevice> getMapMountedUsb(Context cxt) {
        HashMap<String, StorageDevice> resMapStorage = new HashMap<>();
        HashMap<String, StorageDevice> tmpMapStorage = getSDCardInfos(cxt);
        if (tmpMapStorage != null) {
            for (StorageDevice storage : tmpMapStorage.values()) {
                if (storage.isMounted() && !"inner".equals(storage.getStorageId())) {
                    resMapStorage.put(storage.getRoot(), storage);
                }
            }
        }
        return resMapStorage;
    }

    /**
     * Get SDCard Information set
     *
     * @param cxt {@link Context}
     * @return HashMap
     */
    @SuppressLint("ObsoleteSdkInt")
    public static HashMap<String, StorageDevice> getSDCardInfos(Context cxt) {
        HashMap<String, StorageDevice> mapStorageDevices;
        // SDK >= 14
        if (Build.VERSION.SDK_INT >= 14) {
            mapStorageDevices = getSDCardInfo_GreaterOrEqual14(cxt);
        } else {
            mapStorageDevices = getSDCardInfo_Below14();
        }

        //Set uuid
        if (mapStorageDevices != null) {
            Map<String, String> mapUUIDs = Utils.getStorageUUID(cxt);
            for (StorageDevice storage : mapStorageDevices.values()) {
                String tmpUUID = mapUUIDs.get(storage.getRoot());
                if (tmpUUID != null) {
                    storage.setStorageId(tmpUUID);
                }
            }
        }
        return mapStorageDevices;
    }

    /**
     * Get exist root path array
     *
     * @param context {@link Context}
     * @return String[]
     */
    public static String[] getExistRoots(Context context) {
        String[] existRoots = null;
        try {
            Map<String, StorageDevice> mapSdInfos = getSDCardInfos(context.getApplicationContext());
            if (mapSdInfos != null) {
                Object[] objSdInfos = mapSdInfos.values().toArray();
                existRoots = new String[objSdInfos.length];
                for (int idx = 0; idx < objSdInfos.length; idx++) {
                    StorageDevice sdInfo = (StorageDevice) objSdInfos[idx];
                    existRoots[idx] = sdInfo.getRoot();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "ERROR :: " + e.getMessage());
            e.printStackTrace();
        }
        return existRoots;
    }

    /**
     * API14以下通过读取Linux的vold.fstab文件来获取SDCard信息
     */
    private static HashMap<String, StorageDevice> getSDCardInfo_Below14() {
        HashMap<String, StorageDevice> sdCardInfos = new HashMap<>();
        BufferedReader bufferedReader = null;
        List<String> dev_mountStrs = null;
        try {
            // API14以下通过读取Linux的vold.fstab文件来获取SDCard信息
            bufferedReader = new BufferedReader(new FileReader(Environment.getRootDirectory().getAbsoluteFile() + File.separator
                    + "etc" + File.separator + "vold.fstab"));
            dev_mountStrs = new ArrayList<String>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("dev_mount")) {
                    dev_mountStrs.add(line);
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //
        String envAbsolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        for (int i = 0; dev_mountStrs != null && i < dev_mountStrs.size(); i++) {
            StorageDevice sdCardInfo = new StorageDevice();
            String[] infoStr = dev_mountStrs.get(i).split(" ");
            sdCardInfo.setLabel(infoStr[1]);
            sdCardInfo.setRoot(infoStr[2]);
            if (sdCardInfo.getRoot().equals(envAbsolutePath)) {
                sdCardInfo.setMounted((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)));
                sdCardInfos.put(SDCARD_INTERNAL, sdCardInfo);
            } else if (sdCardInfo.getRoot().startsWith("/mnt") && !sdCardInfo.getRoot().equals(envAbsolutePath)) {
                File file = new File(sdCardInfo.getRoot() + File.separator + "temp");
                if (file.exists()) {
                    sdCardInfo.setMounted(true);
                } else {
                    if (file.mkdir()) {
                        file.delete();
                        sdCardInfo.setMounted(true);
                    } else {
                        sdCardInfo.setMounted(false);
                    }
                }
                sdCardInfos.put(SDCARD_EXTERNAL, sdCardInfo);
            }
        }
        return sdCardInfos;
    }

    /**
     * Get SDCard Informations that SDK Version >= 14
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static HashMap<String, StorageDevice> getSDCardInfo_GreaterOrEqual14(Context context) {
        HashMap<String, StorageDevice> sdCardInfos = new HashMap<>();
        String[] storagePathList = null;
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumePaths = storageManager.getClass().getMethod("getVolumePaths");
            storagePathList = (String[]) getVolumePaths.invoke(storageManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (storagePathList != null) {
            int loop = storagePathList.length;
            for (int idx = 0; idx < loop; idx++) {
                // Inner SDCard
                if (idx == 0) {
                    String mSDCardPath = storagePathList[0];
                    StorageDevice internalDevInfo = new StorageDevice();
                    internalDevInfo.setRoot(mSDCardPath);
                    internalDevInfo.setMounted(checkSDCardMount14(context, mSDCardPath));
                    sdCardInfos.put(SDCARD_INTERNAL, internalDevInfo);
                } else {
                    String externalDevPath = storagePathList[idx];
                    StorageDevice externalDevInfo = new StorageDevice();
                    externalDevInfo.setRoot(externalDevPath);
                    externalDevInfo.setMounted(checkSDCardMount14(context, externalDevPath));
                    sdCardInfos.put(SDCARD_EXTERNAL + "_" + idx, externalDevInfo);
                }
            }
        }
        return sdCardInfos;
    }

    /**
     * 判断SDCard是否挂载上,返回值为true证明挂载上了，否则未挂载
     *
     * @param context    上下文
     * @param mountPoint 挂载点
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static boolean checkSDCardMount14(Context context, String mountPoint) {
        if (mountPoint == null) {
            return false;
        }
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method getVolumeState = storageManager.getClass().getMethod("getVolumeState", String.class);
            String state = (String) getVolumeState.invoke(storageManager, mountPoint);
            return Environment.MEDIA_MOUNTED.equals(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
