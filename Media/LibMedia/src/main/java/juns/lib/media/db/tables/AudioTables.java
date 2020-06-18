package juns.lib.media.db.tables;

import juns.lib.media.db.tables.MediaTables.MediaInfoTable;

/**
 * 表信息
 *
 * @author Jun.Wang
 */
public interface AudioTables {

    /**
     * Audio information table
     */
    interface AudioInfoTable extends MediaInfoTable {
        // Table Name
        String T_NAME = "AudioInfo";

//        /**
//         * ID
//         * <p>Local index is negative.</p>
//         * <p>If media is from online, id will be got from online; But online index is positive; </p>
//         */
//        String ID = "id";

//        /**
//         * <p>Media file ,such as "../11.mp3".</p>
//         * <p>Parse media attribute, if the attribute title is "Die Another Day", TITLE="Die Another Day"</p>
//         * <p>If the attribute is null or attribute title is null, TITLE="11"</p>
//         */
//        String TITLE = "title";
//        String TITLE_PINYIN = "titlePinYin";
//        // Media File Display Name,like "../11.mp3", "11.mp3" is file name.
//        String FILE_NAME = "fileName";

        // Album ID
        String ALBUM_ID = "albumID";
        // Album Name
        String ALBUM = "album";
        String ALBUM_PINYIN = "albumPinYin";

        // Media Artist
        String ARTIST = "artist";
        String ARTIST_PINYIN = "artistPinYin";

        //        /**
//         * 存储设备标识符，如UUID，这里是指能够唯一标识存储设备的.
//         * <p>目的是为了根据UUID判断当前的存储设备是哪个？</p>
//         */
//        String STORAGE_ID = "storageId";
//        /**
//         * 媒体文件对应的存储设备路径
//         */
//        String ROOT_PATH = "rootPath";
//        /**
//         * <p>MEDIA_URL format,such as "/sdcard/音乐/我是谁.mp3".</p>
//         * <p>MEDIA_FOLDER_PATH "/sdcard/音乐".</p>
//         * <p>MEDIA_FOLDER_NAME "音乐".</p>
//         * <p>MEDIA_FOLDER_NAME_PINYIN "yinyue".</p>
//         */
//        String MEDIA_URL = "mediaUrl";
//        String MEDIA_FOLDER_PATH = "mediaFolderPath";
//        String MEDIA_FOLDER_NAME = "mediaFolderName";
//        String MEDIA_FOLDER_NAME_PINYIN = "mediaFolderNamePinYin";

        // Media Duration
        String DURATION = "duration";

        // Is Collected
        String COLLECTED = "collected";

        // Media CoverImage URL
        String COVER_URL = "coverUrl";

        // Media Lyric
        String LYRIC = "lyric";

//        // Record Create Time
//        String CREATE_TIME = "createTime";
//        // Record Update Time
//        String UPDATE_TIME = "updateTime";

        // Table Create SQL
        String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + T_NAME
                + "("
                + MediaInfoTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                + MediaInfoTable.TITLE + " TEXT NOT NULL,"
                + MediaInfoTable.TITLE_PINYIN + " TEXT,"
                + MediaInfoTable.FILE_NAME + " TEXT,"

                + ALBUM_ID + " LONG,"
                + ALBUM + " TEXT,"
                + ALBUM_PINYIN + " TEXT,"

                + ARTIST + " TEXT,"
                + ARTIST_PINYIN + " TEXT,"

                + MediaInfoTable.STORAGE_ID + " TEXT,"
                + MediaInfoTable.ROOT_PATH + " TEXT NOT NULL,"
                + MediaInfoTable.MEDIA_URL + " TEXT UNIQUE NOT NULL,"
                + MediaInfoTable.MEDIA_FOLDER_PATH + " TEXT NOT NULL,"
                + MediaInfoTable.MEDIA_FOLDER_NAME + " TEXT,"
                + MediaInfoTable.MEDIA_FOLDER_NAME_PINYIN + " TEXT,"

                + DURATION + " INTEGER DEFAULT 0,"
                + COLLECTED + " INTEGER DEFAULT 0,"

                + COVER_URL + " TEXT,"
                + LYRIC + " TEXT,"

                + MediaInfoTable.CREATE_TIME + " LONG,"
                + MediaInfoTable.UPDATE_TIME + " LONG"
                + ")";
    }

    /**
     * Audio sheet table
     */
    interface AudioSheetTable {
        // Table Name
        String T_NAME = "AudioSheet";

        /**
         * ID
         * <p>Local index is negative.</p>
         * <p>Online index is positive.</p>
         */
        String ID = "id";

        /**
         * Sheet title
         */
        String TITLE = "title";
        String TITLE_PINYIN = "titlePinYin";

        // Record Create Time
        String CREATE_TIME = "createTime";
        // Record Update Time
        String UPDATE_TIME = "updateTime";

        // Table Create SQL
        String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + T_NAME
                + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TITLE + " TEXT UNIQUE NOT NULL,"
                + TITLE_PINYIN + " TEXT,"
                + CREATE_TIME + " LONG,"
                + UPDATE_TIME + " LONG"
                + ")";
    }

    /**
     * {@link AudioSheetTable} and {@link AudioInfoTable} mapping table.
     */
    interface AudioSheetMapInfoTable {
        // Table Name
        String T_NAME = "AudioSheet_Map_AudioInfo";

        /**
         * ID
         * <p>Local index is negative.</p>
         * <p>Online index is positive.</p>
         */
        String ID = "id";

        /**
         * {@link AudioSheetTable#ID}
         */
        String SHEET_ID = "sheetId";

        /**
         * <p>MEDIA_URL format,such as "/sdcard/音乐/我是谁.mp3".</p>
         */
        String MEDIA_URL = "mediaUrl";

        // Record Create Time
        String CREATE_TIME = "createTime";
        // Record Update Time
        String UPDATE_TIME = "updateTime";

        // Table Create SQL
        String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + T_NAME
                + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SHEET_ID + " INTEGER NOT NULL,"
                + MEDIA_URL + " TEXT NOT NULL,"
                + CREATE_TIME + " LONG,"
                + UPDATE_TIME + " LONG"
                + ")";
    }
}