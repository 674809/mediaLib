package juns.lib.media.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import juns.lib.android.utils.Logs;
import juns.lib.media.db.tables.AudioTables;

/**
 * DataBase Create
 *
 * @author Jun.Wang
 */
public class AudioDBHelper extends SQLiteOpenHelper {
    //TAG
    private static final String TAG = "AudioDBHelper";

    /**
     * {@link Context}
     */
    private Context mContext;

    /**
     * Database name
     */
    private String mDbName;

    /**
     * Database version number
     * <p>History version number : NONE</p>
     * <p>Now : 1</p>
     */
    private static final int DB_VERSION = 1;

    /**
     * @param context 上下文环境
     * @param dbName  Format should be similar to "/sdcard/Music/TrAudio.sqlite"
     */
    public AudioDBHelper(Context context, String dbName) {
        super(context, dbName, null, DB_VERSION);
        try {
            mContext = context.getApplicationContext();
            mDbName = dbName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logs.i(TAG, "onCreate(" + db + ")");
        createTables(db, false);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logs.i(TAG, "onUpgrade(" + db + "," + oldVersion + "," + newVersion + ")");
        createTables(db, true);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        Logs.i(TAG, "onDowngrade(" + db + "," + oldVersion + "," + newVersion + ")");
        createTables(db, true);
    }

    private void createTables(SQLiteDatabase db, boolean recreate) {
        if (mContext == null || db == null) {
            return;
        }

        //重新创建数据库表
        if (recreate) {
            //Delete exist
            boolean delRes = mContext.deleteDatabase(mDbName);
            Logs.i(TAG, "delRes : " + delRes);
        }

        //Create new
        db.execSQL(AudioTables.AudioInfoTable.SQL_CREATE);
        db.execSQL(AudioTables.AudioSheetTable.SQL_CREATE);
        db.execSQL(AudioTables.AudioSheetMapInfoTable.SQL_CREATE);
    }
}