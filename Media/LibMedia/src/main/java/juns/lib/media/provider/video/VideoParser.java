package juns.lib.media.provider.video;

import android.database.Cursor;

import juns.lib.media.bean.ProVideo;
import juns.lib.media.db.tables.VideoTables.VideoInfoTable;

/**
 * Media parser.
 * <p>1. Parse cursor to media list.</p>
 *
 * @author Jun.Wang
 */
public class VideoParser {
    public static ProVideo parseFromCursor(Cursor cursor) {
        ProVideo media = null;
        try {
            if (cursor != null && !cursor.isClosed()) {
                media = new ProVideo();
                media.setId(cursor.getInt(cursor.getColumnIndex(VideoInfoTable.ID)));
                media.setTitle(cursor.getString(cursor.getColumnIndex(VideoInfoTable.TITLE)));
                media.setTitlePinYin(cursor.getString(cursor.getColumnIndex(VideoInfoTable.TITLE_PINYIN)));
                media.setFileName(cursor.getString(cursor.getColumnIndex(VideoInfoTable.FILE_NAME)));
                media.setStorageId(cursor.getString(cursor.getColumnIndex(VideoInfoTable.STORAGE_ID)));
                media.setRootPath(cursor.getString(cursor.getColumnIndex(VideoInfoTable.ROOT_PATH)));
                media.setMediaUrl(cursor.getString(cursor.getColumnIndex(VideoInfoTable.MEDIA_URL)));
                media.setMediaFolderPath(cursor.getString(cursor.getColumnIndex(VideoInfoTable.MEDIA_FOLDER_PATH)));
                media.setMediaFolderName(cursor.getString(cursor.getColumnIndex(VideoInfoTable.MEDIA_FOLDER_NAME)));
                media.setMediaFolderNamePinYin(cursor.getString(cursor.getColumnIndex(VideoInfoTable.MEDIA_FOLDER_NAME_PINYIN)));
                media.setDuration(cursor.getInt(cursor.getColumnIndex(VideoInfoTable.DURATION)));
                media.setCollected(cursor.getInt(cursor.getColumnIndex(VideoInfoTable.COLLECTED)));
                media.setCoverUrl(cursor.getString(cursor.getColumnIndex(VideoInfoTable.COVER_URL)));
                media.setWidth(cursor.getInt(cursor.getColumnIndex(VideoInfoTable.WIDTH)));
                media.setHeight(cursor.getInt(cursor.getColumnIndex(VideoInfoTable.HEIGHT)));
                media.setRotation(cursor.getInt(cursor.getColumnIndex(VideoInfoTable.ROTATION)));
                media.setCaption(cursor.getString(cursor.getColumnIndex(VideoInfoTable.CAPTION)));
                media.setCreateTime(cursor.getLong(cursor.getColumnIndex(VideoInfoTable.CREATE_TIME)));
                media.setUpdateTime(cursor.getLong(cursor.getColumnIndex(VideoInfoTable.UPDATE_TIME)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }
}
