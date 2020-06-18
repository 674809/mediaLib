package juns.lib.media.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

import juns.lib.java.utils.CharacterParser;
import juns.lib.java.utils.EmptyUtil;

/**
 * 媒体元数据 - 图片
 *
 * @author Jun.Wang
 */
public class ProImage extends MediaBase implements Parcelable {
//    /**
//     * Program ID
//     */
//    private int id;

//    /**
//     * Program Title
//     *
//     * <p>"/sdcard/媒体/test.mp3" , 通过解析实际名称为"昨夜星辰",那么值即为"昨夜星辰"</p>
//     * <p>如果解析不到实际名称，那么为不含后缀的文件名，即"test"</p>
//     */
//    private String title = "";
//    /**
//     * Spelling
//     */
//    private String titlePinYin = "";
//    /**
//     * 文件名,如"/sdcard/媒体/test.mp3"，文件名为 "test.mp3"
//     */
//    private String fileName = "";

//    /**
//     * 存储设备标识符，如UUID，这里是指能够唯一标识存储设备的.
//     * <p>目的是为了根据UUID判断当前的存储设备是哪个？</p>
//     */
//    private String storageId = "";
//    /**
//     * Storage path.
//     */
//    private String rootPath = "";
//    /**
//     * Media Play URL
//     * <p>e.g. "/sdcard/music/test.mp4"</p>
//     */
//    private String mediaUrl = "";
//    /**
//     * Media folder path
//     * <p>e.g. mediaUrl="/sdcard/music/test.mp4" ; mediaFolderPath="/sdcard/music"</p>
//     */
//    private String mediaFolderPath = "";
//    /**
//     * Media Play URL
//     * <p>e.g. mediaUrl="/sdcard/媒体/test.mp3" ; mediaFolderName="媒体"</p>
//     */
//    private String mediaFolderName = "";
//    /**
//     * Media Play URL
//     * <p>
//     * e.g. mediaUrl="/sdcard/媒体/test.mp3" ;
//     * mediaFolderName="媒体" ;
//     * mediaFolderNamePinYin="meiti"
//     * </p>
//     */
//    private String mediaFolderNamePinYin = "";

//    /**
//     * Records create/update Time
//     */
//    private long createTime, updateTime = 0;

    public ProImage() {
    }

    protected ProImage(Parcel in) {
        readFromParcel(in);
    }

    /**
     * 此方法需要手动创建,并且一定要创建,否则在AIDL文件中定义如下out参数会报错.
     *
     * @param in {@link Parcel}
     */
    public void readFromParcel(Parcel in) {
        //
        id = in.readInt();
        title = in.readString();
        titlePinYin = in.readString();
        fileName = in.readString();
        //
        storageId = in.readString();
        rootPath = in.readString();
        mediaUrl = in.readString();
        mediaFolderPath = in.readString();
        mediaFolderName = in.readString();
        mediaFolderNamePinYin = in.readString();
        //
        createTime = in.readLong();
        updateTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(titlePinYin);
        dest.writeString(fileName);
        //
        dest.writeString(storageId);
        dest.writeString(rootPath);
        dest.writeString(mediaUrl);
        dest.writeString(mediaFolderPath);
        dest.writeString(mediaFolderName);
        dest.writeString(mediaFolderNamePinYin);
        //
        dest.writeLong(createTime);
        dest.writeLong(updateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProImage> CREATOR = new Creator<ProImage>() {
        @Override
        public ProImage createFromParcel(Parcel in) {
            return new ProImage(in);
        }

        @Override
        public ProImage[] newArray(int size) {
            return new ProImage[size];
        }
    };

    /**
     * Construct From Media File
     */
    public ProImage(String mediaPath, File mediaFile) {
        if (mediaFile == null) {
            mediaFile = new File(mediaPath);
            if (!mediaFile.exists()) {
                return;
            }
        }

        //
        fileName = mediaFile.getName();
        if (EmptyUtil.isEmpty(fileName)) {
            title = UNKNOWN;
        } else {
            int lastIdxOfDot = fileName.lastIndexOf(".");
            if (lastIdxOfDot != -1) {
                title = fileName.substring(0, lastIdxOfDot);
            }
        }
        titlePinYin = CharacterParser.getPingYin(title).toUpperCase();

        //
        mediaUrl = mediaPath;
        File parentFile = mediaFile.getParentFile();
        if (parentFile != null) {
            mediaFolderPath = parentFile.getPath();
            mediaFolderName = parentFile.getName();
            mediaFolderNamePinYin = CharacterParser.getPingYin(mediaFolderName).toUpperCase();
        }
    }
}
