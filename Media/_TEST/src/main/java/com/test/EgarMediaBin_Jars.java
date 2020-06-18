package com.test;

import com.test.utils.JsFileUtils;

import java.io.File;

public class EgarMediaBin_Jars {
    public static void main(String[] args) {
        //
        String pathFolderTarget = "D:/WorkSpace/_TEMP";
        String pathComJarFilePath = "build/intermediates/full_jar/debug/createFullJarDebug/full.jar";

        // MediaScanService depend
        String pathScannerFolderTarget = pathFolderTarget + "/MediaScanService_JAR";
        JsFileUtils.createFolder(pathScannerFolderTarget);

        File fPinYinJar = new File("LibCommon/src/main/libs/pinyin4j-2.5.0.jar");
        JsFileUtils.copyFileByChannel(fPinYinJar, new File(pathScannerFolderTarget + "/PinYinJar.jar"));

        File fLibCommon = new File("LibCommon/" + pathComJarFilePath);
        JsFileUtils.copyFileByChannel(fLibCommon, new File(pathScannerFolderTarget + "/LibCommon.jar"));

        File fLibMedia = new File("LibMedia/" + pathComJarFilePath);
        JsFileUtils.copyFileByChannel(fLibMedia, new File(pathScannerFolderTarget + "/LibMedia.jar"));

        File fLibApiScanner = new File("LibApiScanner/" + pathComJarFilePath);
        JsFileUtils.copyFileByChannel(fLibApiScanner, new File(pathScannerFolderTarget + "/LibApiScanner.jar"));


        // MusicPlayService depend
        String pathPlayServiceFolderTarget = pathFolderTarget + "/MusicPlayService_JAR";
        JsFileUtils.createFolder(pathPlayServiceFolderTarget);

        JsFileUtils.copyFileByChannel(fPinYinJar, new File(pathPlayServiceFolderTarget + "/PinYinJar.jar"));
        JsFileUtils.copyFileByChannel(fLibCommon, new File(pathPlayServiceFolderTarget + "/LibCommon.jar"));
        JsFileUtils.copyFileByChannel(fLibMedia, new File(pathPlayServiceFolderTarget + "/LibMedia.jar"));
        JsFileUtils.copyFileByChannel(fLibApiScanner, new File(pathPlayServiceFolderTarget + "/LibApiScanner.jar"));

        File fLibApiMusic = new File("LibApiMusic/" + pathComJarFilePath);
        JsFileUtils.copyFileByChannel(fLibApiMusic, new File(pathPlayServiceFolderTarget + "/LibApiMusic.jar"));

        // MusicPlayerUI depend.
        String pathPlayerUIFolderTarget = pathFolderTarget + "/MusicPlayerUI_JAR";
        JsFileUtils.createFolder(pathPlayerUIFolderTarget);

        JsFileUtils.copyFileByChannel(fPinYinJar, new File(pathPlayerUIFolderTarget + "/PinYinJar.jar"));
        JsFileUtils.copyFileByChannel(fLibCommon, new File(pathPlayerUIFolderTarget + "/LibCommon.jar"));
        JsFileUtils.copyFileByChannel(fLibMedia, new File(pathPlayerUIFolderTarget + "/LibMedia.jar"));
        JsFileUtils.copyFileByChannel(fLibApiScanner, new File(pathPlayerUIFolderTarget + "/LibApiScanner.jar"));
        JsFileUtils.copyFileByChannel(fLibApiMusic, new File(pathPlayerUIFolderTarget + "/LibApiMusic.jar"));

        File fLetterSideBar = new File("LetterSideBar/" + pathComJarFilePath);
        JsFileUtils.copyFileByChannel(fLetterSideBar, new File(pathPlayerUIFolderTarget + "/LetterSideBar.jar"));

        File fXSkinLoader = new File("XSkinLoader-Lib/" + pathComJarFilePath);
        JsFileUtils.copyFileByChannel(fXSkinLoader, new File(pathPlayerUIFolderTarget + "/XSkinLoader.jar"));
    }
}