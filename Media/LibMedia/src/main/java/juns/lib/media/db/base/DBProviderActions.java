package juns.lib.media.db.base;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;

import juns.lib.media.db.tables.AudioTables;

/**
 * Database operation actions of provider.
 *
 * @author Jun.Wang
 */
public interface DBProviderActions {
    /**
     * Query medias
     *
     * @param columns       列数组
     * @param selection     条件表达式 , "a=?"
     * @param selectionArgs 条件参数
     * @param groupBy       分组
     * @param having        ??
     * @param orderBy       排序条件
     * @return Cursor-{@link AudioTables.AudioInfoTable}
     */
    Cursor queryMedias(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);

    /**
     * Delete medias
     *
     * @param selection     条件表达式 , "a=?"
     * @param selectionArgs 条件参数
     * @return @return int, result.
     */
    int deleteMedias(@Nullable String selection, @Nullable String[] selectionArgs);

    /**
     * Update medias
     *
     * @param values        需要更新的键值对
     * @param selection     条件表达式 , "a=?"
     * @param selectionArgs 条件参数
     * @return Cursor-{@link AudioTables.AudioInfoTable}
     */
    int updateMedias(@Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs);

    /**
     * Query media sheets
     *
     * @param projection    列数组
     * @param selection     条件表达式 , "a=?"
     * @param selectionArgs 条件参数
     * @param sortOrder     排序条件
     * @return Cursor-{@link AudioTables.AudioSheetTable}
     */
    Cursor queryMediaSheets(@Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder);

    /**
     * Insert media sheets
     *
     * @param values {@link ContentValues}
     * @return row id if inserted.
     */
    long insertNewMediaSheets(ContentValues values);

    /**
     * Update media sheets
     *
     * @param values        要修改的键值
     * @param selection     条件表达式 , "a=?"
     * @param selectionArgs 条件参数
     * @return 受到影响的行号
     */
    long updateMediaSheets(@Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs);

    /**
     * Query [media sheet map media information]s
     *
     * @param projection    列数组
     * @param selection     条件表达式 , "a=?"
     * @param selectionArgs 条件参数
     * @param sortOrder     排序条件
     * @return Cursor-{@link AudioTables.AudioSheetMapInfoTable}
     */
    Cursor queryMediaSheetMapInfos(@Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder);

    /**
     * Insert [media sheet map media information]s
     *
     * @param values {@link ContentValues}
     * @return row id if inserted.
     */
    long insertNewMediaSheetMapInfos(ContentValues values);

    /**
     * Delete [media sheet map media information]s
     *
     * @param where     条件表达式 , "a=?"
     * @param whereArgs 条件参数
     * @return long，成功删除的行数
     */
    long deleteMediaSheetMapInfos(String where, String[] whereArgs);
}
