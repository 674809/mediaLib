package juns.lib.media.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.File;

import juns.lib.android.utils.Logs;
import juns.lib.java.utils.CharacterParser;
import juns.lib.java.utils.EmptyUtil;

/**
 * 音频
 *
 * @author Jun.Wang
 */
public class ProAudio extends MediaBase implements Parcelable {
    //TAG
    private static final String TAG = "ProAudio";

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

    /**
     * Album ID
     */
    private long albumID = 0;
    /**
     * Album Name
     */
    private String album = "";
    /**
     * If album="专辑", albumPinYin="zhuanji"
     */
    private String albumPinYin = "";

    /**
     * Artist
     */
    private String artist = "";
    /**
     * If album="艺术家", albumPinYin="yishujia"
     */
    private String artistPinYin = "";

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
     * Words of a song
     */
    private String lyric = "";

//    /**
//     * Records create/update Time
//     */
//    private long createTime, updateTime = 0;

    public ProAudio() {
    }

    protected ProAudio(Parcel in) {
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
        albumID = in.readLong();
        album = in.readString();
        albumPinYin = in.readString();
        artist = in.readString();
        artistPinYin = in.readString();
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
        lyric = in.readString();
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
        dest.writeLong(albumID);
        dest.writeString(album);
        dest.writeString(albumPinYin);
        dest.writeString(artist);
        dest.writeString(artistPinYin);
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
        dest.writeString(lyric);
        //
        dest.writeLong(createTime);
        dest.writeLong(updateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProAudio> CREATOR = new Creator<ProAudio>() {
        @Override
        public ProAudio createFromParcel(Parcel in) {
            return new ProAudio(in);
        }

        @Override
        public ProAudio[] newArray(int size) {
            return new ProAudio[size];
        }
    };

    /**
     * Construct ProMusic From Media File
     */
    public ProAudio(String mediaPath, File mediaFile) {
        if (mediaFile == null) {
            mediaFile = new File(mediaPath);
            if (!mediaFile.exists()) {
                return;
            }
        }

        //
        //sysMediaID
        fileName = mediaFile.getName();
        if (EmptyUtil.isEmpty(fileName)) {
            title = UNKNOWN;
        } else {
            int lastIdxOfDot = fileName.lastIndexOf(".");
            if (lastIdxOfDot != -1) {
                title = fileName.substring(0, lastIdxOfDot);
            }
        }
        titlePinYin = UNKNOWN;

        //
        artist = UNKNOWN;
        artistPinYin = UNKNOWN;

        // albumID
        album = UNKNOWN;
        albumPinYin = UNKNOWN;

        //
        mediaUrl = mediaPath;
        File parentFile = mediaFile.getParentFile();
        if (parentFile != null) {
            mediaFolderPath = parentFile.getPath();
            mediaFolderName = parentFile.getName();
            mediaFolderNamePinYin = UNKNOWN;
        }

        //
        duration = 0;
    }

    /**
     * Parse media information
     *
     * @param context {@link Context}
     * @param media   {@link ProAudio}
     */
    public static void parseMedia(Context context, ProAudio media, File mediaFile) {
        MediaMetadataRetriever mmr = null;
        try {
            //
            if (media == null) {
                return;
            }

            if (mediaFile == null) {
                mediaFile = new File(media.getMediaUrl());
                if (!mediaFile.exists()) {
                    return;
                }
            }

            //
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, Uri.parse(mediaFile.getPath()));

            //
            //sysMediaID
            String parsedTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (!EmptyUtil.isEmpty(parsedTitle)) {
                media.setTitle(parsedTitle);
            }
            media.setTitlePinYin(CharacterParser.getPingYin(media.getTitle()).toUpperCase());
            media.setFileName(mediaFile.getName());

            //
            String parsedArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (!EmptyUtil.isEmpty(parsedArtist)) {
                media.artist = parsedArtist;
            }
            media.artistPinYin = CharacterParser.getPingYin(media.artist).toUpperCase();

            // albumID
            String parsedAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if (!EmptyUtil.isEmpty(parsedAlbum)) {
                media.album = parsedAlbum;
            }
            media.albumPinYin = CharacterParser.getPingYin(media.album).toUpperCase();

            //
//            media.mediaUrl = mediaPath;
            File parentFile = mediaFile.getParentFile();
            if (parentFile != null) {
                media.setMediaFolderPath(parentFile.getPath());
                media.setMediaFolderName(parentFile.getName());
                media.setMediaFolderNamePinYin(CharacterParser.getPingYin(media.getMediaFolderName()).toUpperCase());
            }

            //
//            long fileSize = file.length();
//            long bitRate = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
//            media.duration = (fileSize * 8) / (bitRate) * 1000;
            String strDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (TextUtils.isDigitsOnly(strDuration)) {
                media.duration = Integer.parseInt(strDuration);
            }
        } catch (Exception e) {
            Logs.debugI(TAG, "[Parse Fail] *** {mediaUrl : " + media.getMediaUrl() + "}");
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }

    public static String getThumbNailPath(Context context, String coverImgPath, String mediaUrl) {
        File coverImgFile = new File(coverImgPath);
        if (coverImgFile.exists()) {
            return coverImgPath;
        } else {
            Bitmap coverBitmap = getThumbNail(context, mediaUrl);
            if (coverBitmap != null) {
                storeBitmap(coverImgPath, coverBitmap);
                return coverImgPath;
            }
        }
        return "";
    }

    /**
     * Get video thumbnail
     */
    public static Bitmap getThumbNail(Context context, String mediaUrl) {
        MediaMetadataRetriever mmr = null;
        try {
            //
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, Uri.parse(mediaUrl));

            //
            byte[] picture = mmr.getEmbeddedPicture();
            if (picture != null) {
                return BitmapFactory.decodeByteArray(picture, 0, picture.length);
            }
        } catch (Exception e) {
            Logs.debugI(TAG, "Parse audio cover image ....!!!!");
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
        return null;
    }

    public long getAlbumID() {
        return albumID;
    }

    public void setAlbumID(long albumID) {
        this.albumID = albumID;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumPinYin() {
        return albumPinYin;
    }

    public void setAlbumPinYin(String albumPinYin) {
        this.albumPinYin = albumPinYin;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtistPinYin() {
        return artistPinYin;
    }

    public void setArtistPinYin(String artistPinYin) {
        this.artistPinYin = artistPinYin;
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

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }
}
