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

import org.OneEducation.HarvestClient.HarvestEntry;

public class HarvestStore extends SQLiteOpenHelper {

    private static final Integer DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "harvest";

    private String QUERY_CREATE = "CREATE TABLE IF NOT EXISTS " +
                                  "entries " +
                                  "(package TEXT, " +
				  "started INTEGER, " +
                                  "duration INTEGER, " +
                                  "PRIMARY KEY (package, started))";

    private String QUERY_DROP = "DROP TABLE IF EXISTS entries";

    private String QUERY_FIND = "SELECT * FROM entries WHERE package = ? AND started = ?";

    private String QUERY_SELECT = "SELECT * FROM entries WHERE started >= ?";

    private String TABLE_NAME = "entries";
    private String COLUMN_PACKAGE = "package";
    private String COLUMN_STARTED = "started";
    private String COLUMN_DURATION = "duration";

    public HarvestStore(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("HarvestStore", "created");
        db.execSQL(QUERY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("HarvestStore", "onUpgrade");
        db.execSQL(QUERY_DROP);
        this.onCreate(db);
    }

    public void persist(HarvestEntry entry) {
        Log.d("HarvestStore", "persist");
        SQLiteDatabase db = this.getWritableDatabase();

        Long previousDuration = 0L;
        Cursor cursor = db.rawQuery(QUERY_FIND, new String[] {entry.packageName, entry.started.toString()});
        if (cursor.moveToFirst()) {
           previousDuration = Long.parseLong(cursor.getString(2), 10);
        }
        cursor.close(); 

        ContentValues values = new ContentValues();
        values.put(COLUMN_PACKAGE, entry.packageName);
        values.put(COLUMN_STARTED, entry.started);
        values.put(COLUMN_DURATION, entry.duration + previousDuration); 

        db.replace(TABLE_NAME, null, values);
        db.close();
    }

    public List<HarvestEntry> retrieve(Long started) {
        Log.d("HarvestStore", "retrieve");
        List<HarvestEntry> entries = new ArrayList<HarvestEntry>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(QUERY_SELECT, new String[] {started.toString()});
        if (cursor.moveToFirst()) {
            do {
                HarvestEntry entry = new HarvestEntry(cursor.getString(0));
                entry.started = Long.parseLong(cursor.getString(1), 10);
                entry.duration = Long.parseLong(cursor.getString(2), 10);

                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();

        db.close();
        return entries;
    }
}
