package juns.lib.media.db.tables;

import juns.lib.media.db.tables.MediaTables.MediaInfoTable;

/**
 * 表信息
 *
 * @author Jun.Wang
 */
public interface ImageTables {
    /**
     * Media information table
     */
    interface ImageInfoTable extends MediaInfoTable {
        // Table Name
        String T_NAME = "ImageInfo";

//        // ID
//        String ID = "id";

//        /**
//         * <p>Media file ,such as "../11.mp3", TITLE="11"</p>
//         */
//        String TITLE = "TITLE";
//        String TITLE_PINYIN = "titlePinYin";
//        // Media File Display Name,like "../11.mp3", "11.mp3" is file name.
//        String FILE_NAME = "fileName";

//        /**
//         * 存储设备标识符，如UUID，这里是指能够唯一标识存储设备的.
//         * <p>目的是为了根据UUID判断当前的存储设备是哪个？</p>
//         */
//        String STORAGE_ID = "storageId";
//        /**
//         * 媒体文件对应的存储设备路径
//         */
//        String ROOT_PATH = "rootPath";
//        // Media URL
//        String MEDIA_URL = "mediaUrl";
//        String MEDIA_FOLDER_PATH = "mediaFolderPath";
//        String MEDIA_FOLDER_NAME = "mediaFolderName";
//        String MEDIA_FOLDER_NAME_PINYIN = "mediaFolderNamePinYin";

//        // Record Create Time
//        String CREATE_TIME = "createTime";
//        // Record Update Time
//        String UPDATE_TIME = "updateTime";

        // Table Create SQL
        String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + T_NAME
                + "("
                + MediaInfoTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                + MediaInfoTable.TITLE + " TEXT,"
                + MediaInfoTable.TITLE_PINYIN + " TEXT,"
                + MediaInfoTable.FILE_NAME + " TEXT,"

                + MediaInfoTable.STORAGE_ID + " TEXT,"
                + MediaInfoTable.ROOT_PATH + " TEXT,"
                + MediaInfoTable.MEDIA_URL + " TEXT UNIQUE NOT NULL,"
                + MediaInfoTable.MEDIA_FOLDER_PATH + " TEXT NOT NULL,"
                + MediaInfoTable.MEDIA_FOLDER_NAME + " TEXT,"
                + MediaInfoTable.MEDIA_FOLDER_NAME_PINYIN + " TEXT,"

                + MediaInfoTable.CREATE_TIME + " LONG,"
                + MediaInfoTable.UPDATE_TIME + " LONG"
                + ")";
    }
}