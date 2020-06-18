package com.wind.me.xskinloader.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.wind.me.xskinloader.entity.SkinConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by Windy on 2018/1/11.
 */

public class AssetFileUtils {

    public static boolean copyAssetFile(Context context, String originAssetFileName, String destFileDirectory,
                                        String destFileName) {
        long startTime = System.currentTimeMillis();
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            is = context.getAssets().open(originAssetFileName);

            File destPathFile = new File(destFileDirectory);
            if (!destPathFile.exists()) {
                destPathFile.mkdirs();
            }

            File destFile = new File(destFileDirectory + File.separator + destFileName);
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(destFile);
            bos = new BufferedOutputStream(fos);

            byte[] buffer = new byte[256];
            int length = 0;
            while ((length = is.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (SkinConfig.DEBUG) {
                Log.e("AssetFileUtils", "copyAssetFile time = " + (System.currentTimeMillis() - startTime));
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != bos) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * 获取单个文件的MD5值
     *
     * @param path the path
     * @return file md 5
     */
    public static String getFileMD5(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 获得assets目录下assetsFileName文件的MD5值
     *
     * @param context        the context
     * @param assetsFileName the assets file mName
     * @return the asset file md 5
     */
    public static String getAssetFileMD5(Context context, String assetsFileName) {
        MessageDigest digest = null;
        InputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = context.getAssets().open(assetsFileName);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 判断assetsFileName文件和targetPath文件的MD5值是否相同
     *
     * @param context        the context
     * @param assetsFilePath the assets file path
     * @param targetPath     the target path
     * @return true MD5值相同
     */
    public static boolean isSameFile(Context context, String assetsFilePath, String targetPath) {
        String assetFileMD5 = getAssetFileMD5(context, assetsFilePath);
        String targetFileMD5 = getFileMD5(targetPath);
        return !TextUtils.isEmpty(assetFileMD5) && !TextUtils.isEmpty(targetFileMD5) && TextUtils.equals(assetFileMD5, targetFileMD5);
    }
}
