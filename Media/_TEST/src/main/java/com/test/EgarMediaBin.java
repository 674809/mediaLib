package com.test;

import com.test.utils.JsFileUtils;

import java.io.File;

public class EgarMediaBin {
    public static void main(String[] args) {
        processEgarMedia();
    }

    private static void processEgarMedia() {
        final String PATH_SRC = "D:/WorkSpace/Code/Studio";
        final String PATH_TEMP = "D:/WorkSpace/_TEMP";

        // Copy root
        System.out.println(" &&&&&&&&&&&&&&& ----Process---- -START- &&&&&&&&&&&&&&&");
        String rootFolderName = "EgarMedia";
        String srcPath = PATH_SRC + "/" + rootFolderName;
        String targetPath = PATH_TEMP + "/" + rootFolderName;
        loopCopy(srcPath, targetPath);
        System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" + " - copy successfully!!! - ");

        // ---- 1. MusicPlayService ----
        // Copy LibApiMusic
        System.out.println(" ^^^^ 1. copy MusicPlayService -START- ^^^^");
        String targetFolder = "MusicPlayService";
        targetPath = PATH_TEMP + "/" + targetFolder;

        String srcFolderToCopy = "LibApiMusic";
        srcPath = PATH_TEMP + "/" + rootFolderName + "/" + srcFolderToCopy;
        JsFileUtils.copyFolder(srcPath, targetPath + "/" + srcFolderToCopy);
        JsFileUtils.deleteFiles(new File(srcPath));
        System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" + " - copy successfully!!! - ");

        String comMkName = "Android.mk";
        File srcMkFile = new File(PATH_SRC + "/" + rootFolderName + "/" + comMkName);
        JsFileUtils.copyFileByChannel(srcMkFile, new File(targetPath + "/" + comMkName));

        // Copy LibMedia
        srcFolderToCopy = "MusicPlayService";
        srcPath = PATH_TEMP + "/" + rootFolderName + "/" + srcFolderToCopy;
        JsFileUtils.copyFolder(srcPath, targetPath + "/" + srcFolderToCopy);
        JsFileUtils.deleteFiles(new File(srcPath));
        System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" + " - copy successfully!!! - ");

        // ---- 2. MusicUI ----
        // Copy LetterSideBar
        System.out.println(" ^^^^ 2. copy MusicUI -START- ^^^^");
        targetFolder = "MusicUI";
        targetPath = PATH_TEMP + "/" + targetFolder;

        srcFolderToCopy = "LetterSideBar";
        srcPath = PATH_TEMP + "/" + rootFolderName + "/" + srcFolderToCopy;
        JsFileUtils.copyFolder(srcPath, targetPath + "/" + srcFolderToCopy);
        JsFileUtils.deleteFiles(new File(srcPath));
        System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" + " - copy successfully!!! - ");

        srcMkFile = new File(PATH_SRC + "/" + rootFolderName + "/" + comMkName);
        JsFileUtils.copyFileByChannel(srcMkFile, new File(targetPath + "/" + comMkName));

        // Copy MusicPlayer_Skin_IOS
        srcFolderToCopy = "MusicPlayer_Skin_IOS";
        srcPath = PATH_TEMP + "/" + rootFolderName + "/" + srcFolderToCopy;
        JsFileUtils.copyFolder(srcPath, targetPath + "/" + srcFolderToCopy);
        JsFileUtils.deleteFiles(new File(srcPath));
        System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" + " - copy successfully!!! - ");

        // Copy XSkinLoader-Lib
        srcFolderToCopy = "XSkinLoader-Lib";
        srcPath = PATH_TEMP + "/" + rootFolderName + "/" + srcFolderToCopy;
        JsFileUtils.copyFolder(srcPath, targetPath + "/" + srcFolderToCopy);
        JsFileUtils.deleteFiles(new File(srcPath));
        System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" + " - copy successfully!!! - ");

        // Copy MusicPlayerUI
        srcFolderToCopy = "MusicPlayerUI";
        srcPath = PATH_TEMP + "/" + rootFolderName + "/" + srcFolderToCopy;
        JsFileUtils.copyFolder(srcPath, targetPath + "/" + srcFolderToCopy);
        JsFileUtils.deleteFiles(new File(srcPath));
        System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" + " - copy successfully!!! - ");

        // Copy MusicPlayerUICom
        // srcFolderToCopy = "MusicPlayerUICom";
        // srcPath = PATH_TEMP + "/" + rootFolderName + "/" + srcFolderToCopy;
        // JsFileUtils.copyFolder(srcPath, targetPath + "/" + srcFolderToCopy);
        // JsFileUtils.deleteFiles(new File(srcPath));
        // System.out.println("[" + srcPath + "] >>> [" + targetPath + "]" +
        // " - copy successfully!!! - ");

        // ---- 3. MediaScanService ----
        // Copy MediaScanService
        System.out.println(" ^^^^ 3. copy MediaScanService -START- ^^^^");
        srcFolderToCopy = "MediaScanService";
        JsFileUtils.renameFile(PATH_TEMP, rootFolderName, srcFolderToCopy);
        System.out.println("[" + PATH_TEMP + "] rename [" + rootFolderName + "] to [" + srcFolderToCopy
                + "] - successfully!!! - ");
        System.out.println(" &&&&&&&&&&&&&&& ----Process---- -END- &&&&&&&&&&&&&&&");
    }

    public static void loopCopy(String srcPath, String targetPath) {
        try {
            //
            File srcFile = new File(srcPath);
            if (isUseless(srcFile.getName())) {
                return;
            }
            if (srcFile.isFile()) {
                JsFileUtils.copyFileByChannel(srcFile, new File(targetPath));
                return;
            }

            //
            JsFileUtils.createFolder(targetPath);
            String[] strFNames = srcFile.list();
            if (strFNames == null) {
                return;
            }

            //
            for (int idx = 0; idx < strFNames.length; idx++) {
                String fName = strFNames[idx];
                String tmpSrcPath = srcPath + "/" + fName;
                File tempSrcFile = new File(tmpSrcPath);
                if (isUseless(tempSrcFile.getName())) {
                    continue;
                }

                // Copy child files
                if (tempSrcFile.isFile()) {
                    JsFileUtils.copyFileByChannel(tempSrcFile, new File(targetPath + "/" + strFNames[idx]));
                    // Loop copy child folders
                } else {
                    String tmpTargetPath = targetPath + "/" + fName;
                    JsFileUtils.createFolder(tmpTargetPath);
                    loopCopy(tmpSrcPath, tmpTargetPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isUseless(String fName) {
        if ("MediaUI".equals(fName) || ".gradle".equals(fName) || ".idea".equals(fName) || "gradle".equals(fName)
                || "build".equals(fName) || "debug".equals(fName) || "release".equals(fName) || "gradlew".equals(fName)
                || "gradle.properties".equals(fName) || "import-summary.txt".equals(fName) || fName.endsWith(".iml")
                || fName.endsWith(".bat") || "local.properties".equals(fName)) {
            return true;
        }
        return false;
    }
}
