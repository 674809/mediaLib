package juns.lib.media.bean;

import java.io.File;

/**
 * 媒体文件封装类
 * <p>将媒体和文件的一些信息映射起来</p>
 *
 * @author Jun.Wang
 */
public class MediaFile {
    private String storageId;
    private String rootPath;
    private File file;

    public MediaFile(String storageId, String rootPath, File file) {
        this.storageId = storageId;
        this.rootPath = rootPath;
        this.file = file;
    }

    public boolean exists() {
        return file != null && file.exists();
    }

    public String getMediaUrl() {
        if (file == null) {
            return "";
        }
        return file.getPath();
    }

    public String getMediaStorePath() {
        if (file != null) {
            File storeFolder = file.getParentFile();
            if (storeFolder != null) {
                return storeFolder.getPath();
            }
        }
        return "";
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
