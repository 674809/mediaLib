package juns.lib.media.bean;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import juns.lib.android.utils.Logs;

/**
 * 媒体基类
 *
 * @author Jun.Wang
 */
public class MediaBase {
    //TAG
    private static final String TAG = "MediaBase";

    //
    public static final String UNKNOWN = "unknown";

    /**
     * Program ID
     */
    protected int id;

    /**
     * Program Title
     *
     * <p>"/sdcard/媒体/test.mp3" , 通过解析实际名称为"昨夜星辰",那么值即为"昨夜星辰"</p>
     * <p>如果解析不到实际名称，那么为不含后缀的文件名，即"test"</p>
     */
    protected String title = "";
    /**
     * Spelling
     */
    protected String titlePinYin = "";
    /**
     * 文件名,如"/sdcard/媒体/test.mp3"，文件名为 "test.mp3"
     */
    protected String fileName = "";

    /**
     * 存储设备标识符，如UUID，这里是指能够唯一标识存储设备的.
     * <p>目的是为了根据UUID判断当前的存储设备是哪个？</p>
     */
    protected String storageId = "";
    /**
     * Storage path.
     */
    protected String rootPath = "";
    /**
     * Media Play URL
     * <p>e.g. "/sdcard/music/test.mp4"</p>
     */
    protected String mediaUrl = "";
    /**
     * Media folder path
     * <p>e.g. mediaUrl="/sdcard/music/test.mp4" ; mediaFolderPath="/sdcard/music"</p>
     */
    protected String mediaFolderPath = "";
    /**
     * Media Play URL
     * <p>e.g. mediaUrl="/sdcard/媒体/test.mp3" ; mediaFolderName="媒体"</p>
     */
    protected String mediaFolderName = "";
    /**
     * Media Play URL
     * <p>
     * e.g. mediaUrl="/sdcard/媒体/test.mp3" ;
     * mediaFolderName="媒体" ;
     * mediaFolderNamePinYin="meiti"
     * </p>
     */
    protected String mediaFolderNamePinYin = "";

    /**
     * Records create/update Time
     */
    protected long createTime, updateTime = 0;

    public static File renameFileWithSpecialName(File file) {
        if (file != null) {
            String fName = file.getName();
            if (fName.contains("'")) {
                fName = fName.replace("'", "`");
                String fPath = file.getParent() + "/" + fName;
                File targetFile = new File(fPath);
                boolean isRenamed = file.renameTo(targetFile);
                if (isRenamed) {
                    return targetFile;
                }
            }
        }
        return file;
    }

    /**
     * 保存Bitmap到SD卡
     *
     * @param filePath  ： 文件路径 ，格式为“.../../example.png”
     * @param bmToStore ： 要执行保存的Bitmap
     */
    public static void storeBitmap(String filePath, Bitmap bmToStore) {
        if (bmToStore != null) {
            // "/sdcard/" + bitName + ".png"
            FileOutputStream fos = null;
            try {
                //
                File targetF = new File(filePath);
                if (targetF.isDirectory() || targetF.exists()) {
                    return;
                }

                //Compress
                File tmpFile = new File(filePath + "_TEMP");
                if (tmpFile.createNewFile()) {
                    fos = new FileOutputStream(tmpFile);
                    bmToStore.compress(Bitmap.CompressFormat.PNG, 100, fos);

                    //Rename
                    if (tmpFile.renameTo(targetF)) {
                        Log.i(TAG, "storeBitmap --END--");
                    }
                }
            } catch (Throwable e) {
                Logs.i(TAG, "storeBitmap() :: " + e.getMessage());
            } finally {
                try {
                    if (fos != null) {
                        // 刷新数据并将数据转交给操作系统
                        fos.flush();
                        // 强制系统缓冲区与基础设备同步
                        // 将系统缓冲区数据写入到文件
                        fos.getFD().sync();
                        fos.close();
                    }
                } catch (Throwable e) {
                    Logs.i(TAG, "storeBitmap()-2- :: " + e.getMessage());

                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitlePinYin() {
        return titlePinYin;
    }

    public void setTitlePinYin(String titlePinYin) {
        this.titlePinYin = titlePinYin;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaFolderPath() {
        return mediaFolderPath;
    }

    public void setMediaFolderPath(String mediaFolderPath) {
        this.mediaFolderPath = mediaFolderPath;
    }

    public String getMediaFolderName() {
        return mediaFolderName;
    }

    public void setMediaFolderName(String mediaFolderName) {
        this.mediaFolderName = mediaFolderName;
    }

    public String getMediaFolderNamePinYin() {
        return mediaFolderNamePinYin;
    }

    public void setMediaFolderNamePinYin(String mediaFolderNamePinYin) {
        this.mediaFolderNamePinYin = mediaFolderNamePinYin;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
