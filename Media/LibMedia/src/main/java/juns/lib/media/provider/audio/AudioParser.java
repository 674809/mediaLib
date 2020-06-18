package juns.lib.media.provider.audio;

import android.database.Cursor;

import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.ProAudioSheet;
import juns.lib.media.bean.ProAudioSheetMapInfo;
import juns.lib.media.db.tables.AudioTables.AudioInfoTable;
import juns.lib.media.db.tables.AudioTables.AudioSheetMapInfoTable;
import juns.lib.media.db.tables.AudioTables.AudioSheetTable;

/**
 * Audio parser.
 * <p>1. Parse cursor to media list.</p>
 *
 * @author Jun.Wang
 */
public class AudioParser {
    public static ProAudio parseFromCursor(Cursor cursor) {
        ProAudio media = null;
        try {
            if (cursor != null && !cursor.isClosed()) {
                media = new ProAudio();
                media.setId(cursor.getInt(cursor.getColumnIndex(AudioInfoTable.ID)));
                media.setTitle(cursor.getString(cursor.getColumnIndex(AudioInfoTable.TITLE)));
                media.setTitlePinYin(cursor.getString(cursor.getColumnIndex(AudioInfoTable.TITLE_PINYIN)));
                media.setFileName(cursor.getString(cursor.getColumnIndex(AudioInfoTable.FILE_NAME)));
                media.setAlbumID(cursor.getLong(cursor.getColumnIndex(AudioInfoTable.ALBUM_ID)));
                media.setAlbum(cursor.getString(cursor.getColumnIndex(AudioInfoTable.ALBUM)));
                media.setAlbumPinYin(cursor.getString(cursor.getColumnIndex(AudioInfoTable.ALBUM_PINYIN)));
                media.setArtist(cursor.getString(cursor.getColumnIndex(AudioInfoTable.ARTIST)));
                media.setArtistPinYin(cursor.getString(cursor.getColumnIndex(AudioInfoTable.ARTIST_PINYIN)));
                media.setStorageId(cursor.getString(cursor.getColumnIndex(AudioInfoTable.STORAGE_ID)));
                media.setRootPath(cursor.getString(cursor.getColumnIndex(AudioInfoTable.ROOT_PATH)));
                media.setMediaUrl(cursor.getString(cursor.getColumnIndex(AudioInfoTable.MEDIA_URL)));
                media.setMediaFolderPath(cursor.getString(cursor.getColumnIndex(AudioInfoTable.MEDIA_FOLDER_PATH)));
                media.setMediaFolderName(cursor.getString(cursor.getColumnIndex(AudioInfoTable.MEDIA_FOLDER_NAME)));
                media.setMediaFolderNamePinYin(cursor.getString(cursor.getColumnIndex(AudioInfoTable.MEDIA_FOLDER_NAME_PINYIN)));
                media.setDuration(cursor.getInt(cursor.getColumnIndex(AudioInfoTable.DURATION)));
                media.setCollected(cursor.getInt(cursor.getColumnIndex(AudioInfoTable.COLLECTED)));
                media.setCoverUrl(cursor.getString(cursor.getColumnIndex(AudioInfoTable.COVER_URL)));
                media.setLyric(cursor.getString(cursor.getColumnIndex(AudioInfoTable.LYRIC)));
                media.setCreateTime(cursor.getLong(cursor.getColumnIndex(AudioInfoTable.CREATE_TIME)));
                media.setUpdateTime(cursor.getLong(cursor.getColumnIndex(AudioInfoTable.UPDATE_TIME)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }

    public static ProAudioSheet parseSheetFromCursor(Cursor cursor) {
        ProAudioSheet mediaSheet = null;
        try {
            if (cursor != null && !cursor.isClosed()) {
                mediaSheet = new ProAudioSheet();
                mediaSheet.setId(cursor.getInt(cursor.getColumnIndex(AudioSheetTable.ID)));
                mediaSheet.setTitle(cursor.getString(cursor.getColumnIndex(AudioSheetTable.TITLE)));
                mediaSheet.setTitlePinYin(cursor.getString(cursor.getColumnIndex(AudioSheetTable.TITLE_PINYIN)));
                mediaSheet.setCreateTime(cursor.getLong(cursor.getColumnIndex(AudioSheetTable.CREATE_TIME)));
                mediaSheet.setUpdateTime(cursor.getLong(cursor.getColumnIndex(AudioSheetTable.UPDATE_TIME)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaSheet;
    }

    public static ProAudioSheetMapInfo parseSheetMapInfoFromCursor(Cursor cursor) {
        ProAudioSheetMapInfo mediaSheetMapInfo = null;
        try {
            if (cursor != null && !cursor.isClosed()) {
                mediaSheetMapInfo = new ProAudioSheetMapInfo();
                mediaSheetMapInfo.setId(cursor.getInt(cursor.getColumnIndex(AudioSheetMapInfoTable.ID)));
                mediaSheetMapInfo.setSheetId(cursor.getInt(cursor.getColumnIndex(AudioSheetMapInfoTable.SHEET_ID)));
                mediaSheetMapInfo.setMediaUrl(cursor.getString(cursor.getColumnIndex(AudioSheetMapInfoTable.MEDIA_URL)));
                mediaSheetMapInfo.setCreateTime(cursor.getLong(cursor.getColumnIndex(AudioSheetMapInfoTable.CREATE_TIME)));
                mediaSheetMapInfo.setUpdateTime(cursor.getLong(cursor.getColumnIndex(AudioSheetMapInfoTable.UPDATE_TIME)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaSheetMapInfo;
    }
}
