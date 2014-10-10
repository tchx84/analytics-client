/*
 * Copyright (C) 2014 Martin Abente Lahaye - martin.abente.lahaye@gmail.com.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.OneEducation.HarvestClient;

import java.util.List;
import java.util.ArrayList;
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

    public List<List<String>> retrieve() {
        Log.i("HarvestClient", "fetching all entries.");
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(QUERY_SELECT, null);

        List<List<String>> entries = new ArrayList<List<String>>();

        if (cursor.moveToFirst()) {
            do {
                List<String> entry = new ArrayList<String>();
                entry.add(cursor.getString(0));
                entry.add(cursor.getString(1));
                entry.add(cursor.getString(2));

                entries.add(entry);
            } while (cursor.moveToNext());
        }

        return entries;
    }
}
