package juns.lib.media.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.File;

import juns.lib.android.utils.Logs;
import juns.lib.android.utils.Utils;
import juns.lib.java.utils.CharacterParser;
import juns.lib.java.utils.EmptyUtil;

/**
 * 视频对象
 *
 * @author Jun.Wang
 */
public class ProVideo extends MediaBase implements Parcelable {
    //TAG
    private static final String TAG = "ProVideo";

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

    /**
     * Program Duration
     */
    private long duration = 0;

    /**
     * Is This Program Collect
     * <p>
     * if == 1 , yes
     * <p>
     * if == 0, not
     */
    private int collected = 0;

    /**
     * Cover Image URL
     */
    private String coverUrl = "";

    /**
     * Video width/height/rotation
     */
    private int width, height, rotation;

    /**
     * 字幕
     */
    private String caption = "";

//    /**
//     * Records create/update Time
//     */
//    private long createTime, updateTime = 0;

    public ProVideo() {
    }

    protected ProVideo(Parcel in) {
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
        duration = in.readLong();
        collected = in.readInt();
        coverUrl = in.readString();
        width = in.readInt();
        height = in.readInt();
        rotation = in.readInt();
        caption = in.readString();
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
        dest.writeLong(duration);
        dest.writeInt(collected);
        dest.writeString(coverUrl);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(rotation);
        dest.writeString(caption);
        //
        dest.writeLong(createTime);
        dest.writeLong(updateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProVideo> CREATOR = new Creator<ProVideo>() {
        @Override
        public ProVideo createFromParcel(Parcel in) {
            return new ProVideo(in);
        }

        @Override
        public ProVideo[] newArray(int size) {
            return new ProVideo[size];
        }
    };

    /**
     * Construct From Media File
     */
    public ProVideo(String mediaPath, File mediaFile) {
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

        //
        duration = 0;
    }

    /**
     * Parse media scale information.
     *
     * @param context {@link Context}
     * @param media   {@link ProVideo}
     */
    public static void parseMediaScaleInfo(Context context, ProVideo media) {
        MediaMetadataRetriever mmr = null;
        try {
            //
            String mediaPath = media.getMediaUrl();
            File file = new File(mediaPath);
            if (file.exists()) {
                //
                mmr = new MediaMetadataRetriever();
                mmr.setDataSource(context, Uri.parse(mediaPath));

                // 视频高度
                media.height = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                // 视频宽度
                media.width = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                // 视频旋转方向
                media.rotation = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            }
        } catch (Exception e) {
            Logs.debugI(TAG, "Parse video scale information failure....!!!!");
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }

    /**
     * Get video thumbnail
     */
    public static Bitmap getThumbNail(String mediaPath, int width, int height, int kind) {
        //Default
        if (width == -1 || height == -1 || kind == -1) {
            return Utils.getVideoThumbnail(mediaPath, 200, 200, MediaStore.Images.Thumbnails.MINI_KIND);
        }
        //Customer
        return Utils.getVideoThumbnail(mediaPath, width, height, kind);
    }

    public static String getThumbNailPath(Context context, String coverImgPath, String mediaUrl) {
        if (context == null) {
            return "";
        }
        File coverImgFile = new File(coverImgPath);
        if (coverImgFile.exists()) {
            return coverImgPath;
        } else {
            Bitmap coverBitmap = getThumbNail(mediaUrl, -1, -1, -1);
            if (coverBitmap != null) {
                storeBitmap(coverImgPath, coverBitmap);
                return coverImgPath;
            }
        }
        return "";
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCollected() {
        return collected;
    }

    public void setCollected(int collected) {
        this.collected = collected;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
