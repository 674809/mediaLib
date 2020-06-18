package juns.lib.media.provider.image;

import android.database.Cursor;

import juns.lib.media.bean.ProImage;
import juns.lib.media.db.tables.ImageTables.ImageInfoTable;

/**
 * Media parser.
 * <p>1. Parse cursor to media list.</p>
 *
 * @author Jun.Wang
 */
public class ImageParser {
    public static ProImage parseFromCursor(Cursor cursor) {
        ProImage media = null;
        try {
            if (cursor != null && !cursor.isClosed()) {
                media = new ProImage();
                media.setId(cursor.getInt(cursor.getColumnIndex(ImageInfoTable.ID)));
                media.setTitle(cursor.getString(cursor.getColumnIndex(ImageInfoTable.TITLE)));
                media.setTitlePinYin(cursor.getString(cursor.getColumnIndex(ImageInfoTable.TITLE_PINYIN)));
                media.setFileName(cursor.getString(cursor.getColumnIndex(ImageInfoTable.FILE_NAME)));
                media.setStorageId(cursor.getString(cursor.getColumnIndex(ImageInfoTable.STORAGE_ID)));
                media.setRootPath(cursor.getString(cursor.getColumnIndex(ImageInfoTable.ROOT_PATH)));
                media.setMediaUrl(cursor.getString(cursor.getColumnIndex(ImageInfoTable.MEDIA_URL)));
                media.setMediaFolderName(cursor.getString(cursor.getColumnIndex(ImageInfoTable.MEDIA_FOLDER_NAME)));
                media.setMediaFolderNamePinYin(cursor.getString(cursor.getColumnIndex(ImageInfoTable.MEDIA_FOLDER_NAME_PINYIN)));
                media.setCreateTime(cursor.getLong(cursor.getColumnIndex(ImageInfoTable.CREATE_TIME)));
                media.setUpdateTime(cursor.getLong(cursor.getColumnIndex(ImageInfoTable.UPDATE_TIME)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }
}
