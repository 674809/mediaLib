package juns.lib.media.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * AudioSheetTable 映射 AudioInfoTable
 *
 * @author Jun.Wang
 */
public class ProAudioSheetMapInfo extends MediaSheetMapInfoBase implements Parcelable {
    private int id;
    private int sheetId;
    private String mediaUrl;
    private long createTime;
    private long updateTime;

    public ProAudioSheetMapInfo() {
    }

    protected ProAudioSheetMapInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(sheetId);
        dest.writeString(mediaUrl);
        dest.writeLong(createTime);
        dest.writeLong(updateTime);
    }

    /**
     * 此方法需要手动创建,并且一定要创建,否则在AIDL文件中定义如下out参数会报错.
     *
     * @param in {@link Parcel}
     */
    public void readFromParcel(Parcel in) {
        id = in.readInt();
        sheetId = in.readInt();
        mediaUrl = in.readString();
        createTime = in.readLong();
        updateTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProAudioSheetMapInfo> CREATOR = new Creator<ProAudioSheetMapInfo>() {
        @Override
        public ProAudioSheetMapInfo createFromParcel(Parcel in) {
            return new ProAudioSheetMapInfo(in);
        }

        @Override
        public ProAudioSheetMapInfo[] newArray(int size) {
            return new ProAudioSheetMapInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSheetId() {
        return sheetId;
    }

    public void setSheetId(int sheetId) {
        this.sheetId = sheetId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
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
