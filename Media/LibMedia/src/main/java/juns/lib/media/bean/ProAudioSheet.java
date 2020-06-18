package juns.lib.media.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Audio sheet
 *
 * @author Jun.Wang
 */
public class ProAudioSheet extends MediaSheetBase implements Parcelable {
    private int id;
    private String title;
    private String titlePinYin;
    private long createTime;
    private long updateTime;

    public ProAudioSheet() {
    }

    protected ProAudioSheet(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(titlePinYin);
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
        title = in.readString();
        titlePinYin = in.readString();
        createTime = in.readLong();
        updateTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProAudioSheet> CREATOR = new Creator<ProAudioSheet>() {
        @Override
        public ProAudioSheet createFromParcel(Parcel in) {
            return new ProAudioSheet(in);
        }

        @Override
        public ProAudioSheet[] newArray(int size) {
            return new ProAudioSheet[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getTitlePinYin() {
        return titlePinYin;
    }

    public void setTitlePinYin(String namePinYin) {
        this.titlePinYin = namePinYin;
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
