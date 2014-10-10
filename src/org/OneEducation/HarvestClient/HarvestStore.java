package org.OneEducation.HarvestClient;

import java.util.List;
import java.lang.Long;
import java.lang.Integer;
import java.lang.String;

import android.util.Log;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class HarvestStore extends SQLiteOpenHelper {

    private static final Integer DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "harvest";

    private String QUERY_CREATE = "CREATE TABLE IF NOT EXISTS " +
                                  "entries " +
                                  "(timestamp INTEGER, " +
				  "package TEXT, " +
                                  "count INTEGER, " +
                                  "PRIMARY KEY (timestamp, package))";

    private String QUERY_DROP = "DROP TABLE IF EXISTS entries";

    private String QUERY_SELECT = "SELECT * FROM entries";

    private String TABLE_NAME = "entries";
    private String COLUMN_TIMESTAMP = "timestamp";
    private String COLUMN_PACKAGE = "package";
    private String COLUMN_COUNT = "count";

    public HarvestStore(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("HarvestClient", "creating DB");
        db.execSQL(QUERY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("HarvestClient", "dropping DB");
        db.execSQL(QUERY_DROP);
        this.onCreate(db);
    }

    public void persist(Long timestamp, String packageName, Long count){
        String message = String.format("persisting: %d, %s, %d", timestamp, packageName, count);
        Log.i("HarvestClient", message);

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_PACKAGE, packageName);
        values.put(COLUMN_COUNT, count);

        db.replace(TABLE_NAME, null, values);
        db.close();
    }

    public void retrieve() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(QUERY_SELECT, null);

        Log.i("HarvestClient", "fetching all entries.");
        if (cursor.moveToFirst()) {
            do {
                String message = String.format("%s, %s, %s", cursor.getString(0), cursor.getString(1), cursor.getString(2));
                Log.i("HarvestClient", message);
            } while (cursor.moveToNext());
        }
    }
}
