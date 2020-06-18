package com.egar.scanner.api;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.FilterFolder;
import juns.lib.media.bean.FilterMedia;
import juns.lib.media.bean.MediaBase;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.ProAudioSheet;
import juns.lib.media.bean.ProAudioSheetMapInfo;
import juns.lib.media.bean.ProImage;
import juns.lib.media.bean.ProVideo;
import juns.lib.media.db.tables.AudioTables.AudioInfoTable;
import juns.lib.media.db.tables.AudioTables.AudioSheetMapInfoTable;
import juns.lib.media.db.tables.AudioTables.AudioSheetTable;
import juns.lib.media.db.tables.MediaTables.MediaInfoTable;
import juns.lib.media.flags.FilterType;
import juns.lib.media.flags.MediaType;
import juns.lib.media.provider.IDataOpActions;
import juns.lib.media.provider.audio.AudioParser;
import juns.lib.media.provider.audio.AudioProviderInfo;
import juns.lib.media.provider.image.ImageParser;
import juns.lib.media.provider.image.ImageProviderInfo;
import juns.lib.media.provider.video.VideoParser;
import juns.lib.media.provider.video.VideoProviderInfo;

public class EgarApiProvider implements IDataOpActions {
    //TAG
    private static final String TAG = "EgarApiProvider";

    private Context mContext;

    public EgarApiProvider(Context context) {
        mContext = context;
    }

    /**
     * @param type 1: Audio;
     *             <p>2: Video;</p>
     *             <p>3: Image.</p>
     *             <p>See {@link MediaType}</p>
     *             <p></p>
     * @return 查询结果{@link Cursor}
     */
    private Cursor getAllMediasCursor(int type) {
        String sortBy = MediaInfoTable.TITLE_PINYIN;
        Uri uri = null;
        switch (type) {
            case MediaType.VIDEO:
                uri = VideoProviderInfo.getUriMediaInfoQueryAll();
                break;
            case MediaType.IMAGE:
                uri = ImageProviderInfo.getUriMediaInfoQueryAll();
                break;
            case MediaType.AUDIO:
                uri = AudioProviderInfo.getUriMediaInfoQueryAll();
                break;
        }
        if (uri == null) {
            return null;
        }

        //
        try {
            ContentResolver cr = mContext.getContentResolver();
            return cr.query(uri, null, null, null, sortBy);
        } catch (Exception e) {
            Logs.i(TAG, "getAllTypeMedias() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param type   See {@link MediaType}
     * @param sortBy See {@link FilterType}
     * @param params 查询条件,统一为Like模糊匹配
     *               <p>params 为null表示查询所有数据</p>
     *               <p>Audios : [0]folderName,[1]mediaName,[2]fileName, [3]artistName,[4]albumName</p>
     *               <p>Videos or Images: [0]folderName,[1]mediaName,[2]fileName</p>
     * @return 查询结果{@link Cursor}
     */
    private Cursor getAllMediasCursor(int type, int sortBy, String[] params) {
        //Uri
        Uri uri = null;
        switch (type) {
            case MediaType.AUDIO:
                uri = AudioProviderInfo.getUriMediaInfoQueryAll();
                break;
            case MediaType.VIDEO:
                uri = VideoProviderInfo.getUriMediaInfoQueryAll();
                break;
            case MediaType.IMAGE:
                uri = ImageProviderInfo.getUriMediaInfoQueryAll();
                break;
        }
        if (uri == null) {
            return null;
        }

        // String[] projection = null;
        String selection = null;
        if (params != null) {
            if (params.length >= 1 && !TextUtils.isEmpty(params[0])) {
                if (params[0].contains("/")) {
                    selection = MediaInfoTable.MEDIA_FOLDER_PATH + "='" + params[0] + "'";
                } else {
                    selection = MediaInfoTable.MEDIA_FOLDER_NAME + "='" + params[0] + "'";
                }
            }
            if (params.length >= 2 && !TextUtils.isEmpty(params[1])) {
                String tmpSelectionPinYing = MediaInfoTable.TITLE_PINYIN+ " like '%" + params[1] + "%'";
                String tmpSelection = MediaInfoTable.TITLE + " like '%" + params[1] + "%'"+" or "+tmpSelectionPinYing;
                if (selection == null) {
                    selection = tmpSelection;
                } else {
                    selection = tmpSelection + " and " + tmpSelection;
                }
            }
            if (params.length >= 3 && !TextUtils.isEmpty(params[2])) {
                String tmpSelection = MediaInfoTable.FILE_NAME + "='" + params[2] + "'";
                if (selection == null) {
                    selection = tmpSelection;
                } else {
                    selection = tmpSelection + " and " + tmpSelection;
                }
            }
            if (type == MediaType.AUDIO) {
                if (params.length >= 4 && !TextUtils.isEmpty(params[3])) {
                    String tmpSelection = AudioInfoTable.ARTIST + "='" + params[3] + "'";
                    if (selection == null) {
                        selection = tmpSelection;
                    } else {
                        selection = tmpSelection + " and " + tmpSelection;
                    }
                }
                if (params.length >= 5 && !TextUtils.isEmpty(params[4])) {
                    String tmpSelection = AudioInfoTable.ALBUM + "='" + params[4] + "'";
                    if (selection == null) {
                        selection = tmpSelection;
                    } else {
                        selection = tmpSelection + " and " + tmpSelection;
                    }
                }
                if (params.length >= 6 && !TextUtils.isEmpty(params[5])) {
                    String tmpSelection = AudioInfoTable.COLLECTED + "=" + params[5];
                    if (selection == null) {
                        selection = tmpSelection;
                    } else {
                        selection = tmpSelection + " and " + tmpSelection;
                    }
                }
            }
        }
        Logs.i(TAG, "getAllMediasCursor() >> [selection: " + selection + "]");

        // String[] selectionArgs = null;
        String sortOrder = null;
        switch (type) {
            case MediaType.AUDIO:
                switch (sortBy) {
                    case FilterType.FOLDER_NAME:
                        sortOrder = AudioInfoTable.MEDIA_FOLDER_NAME_PINYIN;
                        break;
                    case FilterType.MEDIA_NAME:
                        sortOrder = AudioInfoTable.TITLE_PINYIN;
                        break;
                    case FilterType.ARTIST_NAME:
                        sortOrder = AudioInfoTable.ARTIST_PINYIN;
                        break;
                    case FilterType.ALBUM_NAME:
                        sortOrder = AudioInfoTable.ALBUM_PINYIN;
                        break;
                }
                break;
            case MediaType.VIDEO:
            case MediaType.IMAGE:
                switch (sortBy) {
                    case FilterType.FOLDER_NAME:
                        sortOrder = MediaInfoTable.MEDIA_FOLDER_NAME_PINYIN;
                        break;
                    case FilterType.MEDIA_NAME:
                        sortOrder = MediaInfoTable.TITLE_PINYIN;
                        break;
                }
                break;
        }

        //Query
        try {
            ContentResolver cr = mContext.getContentResolver();
            return cr.query(uri, null, selection, null, sortOrder);
        } catch (Exception e) {
            Logs.i(TAG, "getAllMedias() >> e: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List getAllMedias(int type, int sortBy, String[] params) {
        //Response list.
        List<MediaBase> listMedias = new ArrayList<>();
        //Query
        try {
            if (type == MediaType.ALL) {
                try {
                    Cursor cursor = getAllMediasCursor(MediaType.AUDIO);
                    if (cursor != null) {
                        Logs.i(TAG, "getAllMedias(ALL-AUDIO) >> cursorCount: " + cursor.getCount());
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            MediaBase media = AudioParser.parseFromCursor(cursor);
                            if (media != null) {
                                listMedias.add(media);
                            }
                        }
                        cursor.close();
                    }
                    cursor = getAllMediasCursor(MediaType.VIDEO);
                    if (cursor != null) {
                        Logs.i(TAG, "getAllMedias(ALL-VIDEO) >> cursorCount: " + cursor.getCount());
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            MediaBase media = VideoParser.parseFromCursor(cursor);
                            if (media != null) {
                                listMedias.add(media);
                            }
                        }
                        cursor.close();
                    }
                    cursor = getAllMediasCursor(MediaType.IMAGE);
                    if (cursor != null) {
                        Logs.i(TAG, "getAllMedias(ALL-IMAGE) >> cursorCount: " + cursor.getCount());
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            MediaBase media = ImageParser.parseFromCursor(cursor);
                            if (media != null) {
                                listMedias.add(media);
                            }
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Logs.i(TAG, "getAllTypeMedias(ALL) >> e: " + e.getMessage());
                    e.printStackTrace();
                }

            } else {
                Cursor cursor = getAllMediasCursor(type, sortBy, params);
                if (cursor != null) {
                    Logs.i(TAG, "getAllMedias(" + MediaType.desc(type) + ") >> cursorCount: " + cursor.getCount());
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        MediaBase media = null;
                        switch (type) {
                            case MediaType.AUDIO:
                                media = AudioParser.parseFromCursor(cursor);
                                Logs.debugI(TAG, "Audio - name:|" + ((ProAudio) media).getTitle() + "|\n"
                                        + "MediaUrl :|" + ((ProAudio) media).getMediaUrl() + "|");
                                break;
                            case MediaType.VIDEO:
                                media = VideoParser.parseFromCursor(cursor);
                                Logs.debugI(TAG, "Video - name:|" + ((ProVideo) media).getTitle() + "|\n"
                                        + "MediaUrl :|" + ((ProVideo) media).getMediaUrl() + "|");
                                break;
                            case MediaType.IMAGE:
                                media = ImageParser.parseFromCursor(cursor);
                                Logs.debugI(TAG, "Image - name:|" + ((ProImage) media).getTitle() + "|\n"
                                        + "MediaUrl :|" + ((ProImage) media).getMediaUrl() + "|");
                                break;
                        }
                        if (media != null) {
                            listMedias.add(media);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "getAllMedias() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return listMedias;
    }

    @Override
    public List getMediasByColumns(int type, Map<String, String> whereColumns, String sortOrder) {
        //Uri
        Uri uri;
        switch (type) {
            case MediaType.VIDEO:
                uri = VideoProviderInfo.getUriMediaInfoQueryAll();
                break;
            case MediaType.IMAGE:
                uri = ImageProviderInfo.getUriMediaInfoQueryAll();
                break;
            case MediaType.AUDIO:
            default:
                uri = AudioProviderInfo.getUriMediaInfoQueryAll();
                break;
        }

        //Query
        List<MediaBase> list = null;
        //Query
        Cursor cursor = null;
        try {
            StringBuilder selection = new StringBuilder();
            String[] selectionArgs = new String[whereColumns.size()];
            int idx = 0;
            for (String key : whereColumns.keySet()) {
                if (!TextUtils.isEmpty(selection.toString())) {
                    selection.append(" and ");
                }
                selection.append(key).append("=?");
                selectionArgs[idx] = whereColumns.get(key);
            }

            list = new ArrayList<>();
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, null, selection.toString(), selectionArgs, sortOrder);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    MediaBase media = null;
                    switch (type) {
                        case MediaType.AUDIO:
                            media = AudioParser.parseFromCursor(cursor);
                            break;
                        case MediaType.VIDEO:
                            media = VideoParser.parseFromCursor(cursor);
                            break;
                        case MediaType.IMAGE:
                            media = ImageParser.parseFromCursor(cursor);
                            break;
                    }
                    if (media != null) {
                        list.add(media);
                    }
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "getMediasByColumns() >> e: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

    @Override
    public List getFilterFolders(int type) {
        //Uri
        Uri uri = null;
        switch (type) {
            case MediaType.AUDIO:
                uri = AudioProviderInfo.getUriMediaInfoQueryDistinctCols();
                break;
            case MediaType.VIDEO:
                uri = VideoProviderInfo.getUriMediaInfoQueryDistinctCols();
                break;
            case MediaType.IMAGE:
                uri = ImageProviderInfo.getUriMediaInfoQueryDistinctCols();
                break;
        }
        if (uri == null) {
            return null;
        }

        //
        String[] projection = new String[3];
        projection[0] = MediaInfoTable.MEDIA_FOLDER_PATH;
        projection[1] = MediaInfoTable.MEDIA_FOLDER_NAME;
        projection[2] = MediaInfoTable.MEDIA_FOLDER_NAME_PINYIN;

        //Query
        List<FilterFolder> list = null;
        Cursor cursor = null;
        try {
            list = new ArrayList<>();
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, null, null, MediaInfoTable.MEDIA_FOLDER_NAME_PINYIN);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    FilterFolder filterMedia = new FilterFolder();
                    filterMedia.mediaFolder = new File(cursor.getString(cursor.getColumnIndex(projection[0])));
                    filterMedia.sortStr = cursor.getString(cursor.getColumnIndex(projection[1]));
                    filterMedia.sortStrPinYin = cursor.getString(cursor.getColumnIndex(projection[2]));
                    list.add(filterMedia);
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "getFilterFolders() >> e: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

    @Override
    public List getFilterArtists(int type) {
        //Uri
        Uri uri = AudioProviderInfo.getUriMediaInfoQueryDistinctCols();
        if (uri == null) {
            return null;
        }

        //
        String[] projection = new String[2];
        projection[0] = AudioInfoTable.ARTIST;
        projection[1] = AudioInfoTable.ARTIST_PINYIN;

        //Query
        List<FilterMedia> list = null;
        //Query
        Cursor cursor = null;
        try {
            list = new ArrayList<>();
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, null, null, AudioInfoTable.ARTIST_PINYIN);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    FilterMedia filterMedia = new FilterMedia();
                    filterMedia.sortStr = cursor.getString(cursor.getColumnIndex(projection[0]));
                    filterMedia.sortStrPinYin = cursor.getString(cursor.getColumnIndex(projection[1]));
                    list.add(filterMedia);
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "getFilterArtists() >> e: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

    @Override
    public List getFilterAlbums(int type) {
        //Uri
        Uri uri = AudioProviderInfo.getUriMediaInfoQueryDistinctCols();
        if (uri == null) {
            return null;
        }

        //
        String[] projection = new String[3];
        projection[0] = AudioInfoTable.ALBUM;
        projection[1] = AudioInfoTable.ALBUM_PINYIN;
        projection[2] = AudioInfoTable.ALBUM_ID;

        //Query
        List<FilterMedia> list = null;
        //Query
        Cursor cursor = null;
        try {
            list = new ArrayList<>();
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(uri, projection, null, null, AudioInfoTable.ALBUM_PINYIN);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    FilterMedia filterMedia = new FilterMedia();
                    filterMedia.sortStr = cursor.getString(cursor.getColumnIndex(projection[0]));
                    filterMedia.sortStrPinYin = cursor.getString(cursor.getColumnIndex(projection[1]));
                    list.add(filterMedia);
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "getFilterAlbums() >> e: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

    @Override
    public List getAllMediaSheets(int type, int id) {
        //Response list.
        List<ProAudioSheet> listMediaSheets = new ArrayList<>();
        try {
            //uri
            //projection
            String selection = null;
            String[] selectionArgs = null;
            if (id > 0) {
                selection = AudioSheetTable.ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
            }
            //sortOrder
            //
            ContentResolver cr = mContext.getContentResolver();
            Cursor cursor = cr.query(AudioProviderInfo.getUriMediaSheetQueryAll(),
                    null,
                    selection, selectionArgs,
                    AudioSheetTable.TITLE_PINYIN);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    ProAudioSheet mediaSheet = AudioParser.parseSheetFromCursor(cursor);
                    if (mediaSheet != null) {
                        listMediaSheets.add(mediaSheet);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Logs.i(TAG, "getAllMediaSheets() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return listMediaSheets;
    }

    @Override
    public int addMediaSheet(int type, List mediaSheet) {
        int rowId = 0;
        try {
            //Uri
            Uri uri = null;
            switch (type) {
                case MediaType.AUDIO:
                    uri = AudioProviderInfo.getUriMediaSheetInsert();
                    break;
                case MediaType.VIDEO:
                    break;
                case MediaType.IMAGE:
                    break;
            }
            if (uri == null) {
                return 0;
            }

            //ContentValues
            ContentValues values = new ContentValues();
            if (mediaSheet instanceof ProAudioSheet) {
                ProAudioSheet sheet = (ProAudioSheet) mediaSheet;
                values.put(AudioSheetTable.TITLE, sheet.getTitle());
                values.put(AudioSheetTable.TITLE_PINYIN, sheet.getTitlePinYin());
                //Timestamp
                long createTime = System.currentTimeMillis();
                values.put(AudioSheetTable.CREATE_TIME, createTime);
                values.put(AudioSheetTable.UPDATE_TIME, createTime);
            }

            //Insert
            ContentResolver cr = mContext.getContentResolver();
            Uri rowUri = cr.insert(uri, values);
            Log.i(TAG, "rowUri : " + rowUri);
            if (rowUri != null) {
                rowId = (int) ContentUris.parseId(rowUri);
            }
        } catch (Exception e) {
            Logs.i(TAG, "addMediaSheet() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return rowId;
    }

    @Override
    public int updateMediaSheet(int type, List mediaSheet) {
        int rowId = 0;

        //
        Uri uri = null;
        ContentValues values = null;
        String where = null;
        String[] selectionArgs = null;

        switch (type) {
            case MediaType.AUDIO:
                //
                uri = AudioProviderInfo.getUriMediaSheetUpdate();
                //
                ProAudioSheet sheet = (ProAudioSheet) mediaSheet;
                values = new ContentValues();
                values.put(AudioSheetTable.TITLE, sheet.getTitle());
                values.put(AudioSheetTable.TITLE_PINYIN, sheet.getTitlePinYin());
                values.put(AudioSheetTable.UPDATE_TIME, System.currentTimeMillis());
                //
                where = AudioSheetTable.ID + "=?";
                selectionArgs = new String[]{String.valueOf(sheet.getId())};
                break;
            case MediaType.VIDEO:
                break;
            case MediaType.IMAGE:
                break;
        }
        if (uri == null) {
            return 0;
        }

        try {
            //Update.
            ContentResolver cr = mContext.getContentResolver();
            rowId = cr.update(uri, values, where, selectionArgs);
        } catch (Exception e) {
            Logs.i(TAG, "updateMediaSheet() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return rowId;
    }

    @Override
    public List getAllMediaSheetMapInfos(int type, int sheetId) {
        //Response list.
        List<ProAudioSheetMapInfo> listMediaSheetMapInfos = new ArrayList<>();
        try {
            //uri
            //projection
            String selection = null;
            String[] selectionArgs = null;
            if (sheetId > 0) {
                selection = AudioSheetMapInfoTable.SHEET_ID + "=?";
                selectionArgs = new String[]{String.valueOf(sheetId)};
            }
            //sortOrder
            ContentResolver cr = mContext.getContentResolver();
            Cursor cursor = cr.query(AudioProviderInfo.getUriMediaSheetMapInfoQueryAll(), null, selection, selectionArgs, null);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    ProAudioSheetMapInfo MediaSheetMapInfo = AudioParser.parseSheetMapInfoFromCursor(cursor);
                    if (MediaSheetMapInfo != null) {
                        listMediaSheetMapInfos.add(MediaSheetMapInfo);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Logs.i(TAG, "getAllMediaSheetMapInfos() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return listMediaSheetMapInfos;
    }

    @Override
    public int addMediaSheetMapInfos(int type, final List listMapInfos) {
        int insertCount = 0;
        //Uri
        Uri uri = null;
        switch (type) {
            case MediaType.AUDIO:
                uri = AudioProviderInfo.getUriMediaSheetMapInfoInsert();
                break;
            case MediaType.VIDEO:
                break;
            case MediaType.IMAGE:
                break;
        }
        if (uri == null) {
            return 0;
        }

        //ContentValues
        try {
            for (Object objMediaSheetMapInfo : listMapInfos) {
                if (objMediaSheetMapInfo instanceof ProAudioSheetMapInfo) {
                    ContentValues values = new ContentValues();
                    ProAudioSheetMapInfo mediaSheetMapInfo = (ProAudioSheetMapInfo) objMediaSheetMapInfo;
                    values.put(AudioSheetMapInfoTable.SHEET_ID, mediaSheetMapInfo.getSheetId());
                    values.put(AudioSheetMapInfoTable.MEDIA_URL, mediaSheetMapInfo.getMediaUrl());
                    //Timestamp
                    long createTime = System.currentTimeMillis();
                    values.put(AudioSheetMapInfoTable.CREATE_TIME, createTime);
                    values.put(AudioSheetMapInfoTable.UPDATE_TIME, createTime);

                    //Insert
                    ContentResolver cr = mContext.getContentResolver();
                    Uri rowUri = cr.insert(uri, values);
                    Log.i(TAG, "rowUri : " + rowUri);
                    if (rowUri != null) {
                        long rowId = (int) ContentUris.parseId(rowUri);
                        if (rowId > 0) {
                            insertCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "addMediaSheetMapInfos() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return insertCount;
    }

    @Override
    public int deleteMediaSheetMapInfos(int type, int sheetId) {
        int count = 0;

        //
        Uri uri = null;
        String where = null;
        String[] selectionArgs = null;

        switch (type) {
            case MediaType.AUDIO:
                //
                uri = AudioProviderInfo.getUriMediaSheetMapInfoDelete();
                //
                if (sheetId > 0) {
                    where = AudioSheetMapInfoTable.SHEET_ID + "=?";
                    selectionArgs = new String[]{String.valueOf(sheetId)};
                }
                break;
            case MediaType.VIDEO:
                break;
            case MediaType.IMAGE:
                break;
        }
        if (uri == null) {
            return 0;
        }

        try {
            //Update.
            ContentResolver cr = mContext.getContentResolver();
            count = cr.delete(uri, where, selectionArgs);
        } catch (Exception e) {
            Logs.i(TAG, "deleteMediaSheetMapInfos() >> e: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }
}
