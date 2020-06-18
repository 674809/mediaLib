package juns.lib.media.db.base;

import java.util.List;

import juns.lib.java.utils.EmptyUtil;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.db.tables.MediaTables;

/**
 * Media database manger
 * <p>{@link juns.lib.media.db.manager.AudioDBManager} should extends this class.</p>
 * <p>{@link juns.lib.media.db.manager.VideoDBManager} should extends this class.</p>
 * <p>{@link juns.lib.media.db.manager.ImageDBManager} should extends this class.</p>
 *
 * @author Jun.Wang
 */
public abstract class BaseDBManager implements DBActions, DBProviderActions {
//    /**
//     * Mounted storage devices
//     */
//    private List<StorageDevice> mMountedStorages;

    /**
     * Exist devices area
     * <p>String type, format : ('/storage/emulated/0','/storage/udiskA73C-1DD7')</p>
     */
    private String mExistDeviceArea;

    public void setDbPath(String dbPath) {
    }

    @Override
    public void bindMounted(List<StorageDevice> storageDevices) {
//        mMountedStorages = storageDevices;

        //Convert to mExistDeviceArea
        StringBuilder selection = null;
        if (!EmptyUtil.isEmpty(storageDevices)) {
            selection = new StringBuilder("(");
            for (int LOOP = storageDevices.size(), idx = 0; idx < LOOP; idx++) {
                StorageDevice storage = storageDevices.get(idx);
                selection.append("'").append(storage.getRoot()).append("'");
                if (idx < (LOOP - 1)) {
                    selection.append(",");
                }
            }
            selection.append(")");
        }
        if (selection != null) {
            mExistDeviceArea = selection.toString();
        }
    }

    /**
     * Get exist device
     *
     * @param onlyArea true means only return value of {@link #mExistDeviceArea}
     * @return false- "ROOT_PATH in {@link #mExistDeviceArea}"
     */
    protected String getExistDeviceArea(boolean onlyArea) {
        if (onlyArea) {
            return mExistDeviceArea;
        }

        //Get where
        String selection = null;
        if (mExistDeviceArea != null) {
            selection = MediaTables.MediaInfoTable.ROOT_PATH + " in " + mExistDeviceArea;
        }
        return selection;
    }
}
