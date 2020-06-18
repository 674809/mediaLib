package juns.lib.media.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 存储设备
 *
 * @author Jun.Wang
 */
public class StorageDevice implements Parcelable {
    /**
     * 名称
     */
    private String label = "";

    /**
     * 存储设备标识符，如UUID，这里是指能够唯一标识存储设备的.
     * <p>目的是为了根据UUID判断当前的存储设备是哪个？</p>
     */
    private String storageId = "";

    /**
     * Root
     */
    private String root = "";

    /**
     * Is Mounted
     */
    private boolean isMounted = false;

    public StorageDevice() {
    }

    protected StorageDevice(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(storageId);
        dest.writeString(root);
        dest.writeByte((byte) (isMounted ? 1 : 0));
    }

    /**
     * 此方法需要手动创建,并且一定要创建,否则在AIDL文件中定义如下out参数会报错.
     *
     * @param in {@link Parcel}
     */
    public void readFromParcel(Parcel in) {
        label = in.readString();
        storageId = in.readString();
        root = in.readString();
        isMounted = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StorageDevice> CREATOR = new Creator<StorageDevice>() {
        @Override
        public StorageDevice createFromParcel(Parcel in) {
            return new StorageDevice(in);
        }

        @Override
        public StorageDevice[] newArray(int size) {
            return new StorageDevice[size];
        }
    };

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public boolean isMounted() {
        return isMounted;
    }

    public void setMounted(boolean mounted) {
        isMounted = mounted;
    }
}
