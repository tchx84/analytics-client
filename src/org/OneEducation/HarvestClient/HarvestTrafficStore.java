/*
 * Copyright (C) 2015 Martin Abente Lahaye - martin.abente.lahaye@gmail.com.
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

import org.OneEducation.HarvestClient.HarvestTrafficEntry;


public class HarvestTrafficStore extends SQLiteOpenHelper {

    private static final String TAG = "HarvestTrafficStore";
    private static final Integer DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "traffic";

    private String QUERY_CREATE = "CREATE TABLE IF NOT EXISTS " +
                                  "entries " +
                                  "(started INTEGER, " +
				  "received INTEGER, " +
                                  "transmitted INTEGER, " +
                                  "PRIMARY KEY (started))";

    private String QUERY_DROP = "DROP TABLE IF EXISTS entries";

    private String QUERY_FIND = "SELECT * FROM entries WHERE started = ?";

    private String QUERY_SELECT = "SELECT * FROM entries WHERE started >= ?";

    private String TABLE_NAME = "entries";
    private String COLUMN_STARTED = "started";
    private String COLUMN_RECEIVED = "received";
    private String COLUMN_TRANSMITTED = "transmitted";

    public HarvestTrafficStore(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "created");
        db.execSQL(QUERY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade");
        db.execSQL(QUERY_DROP);
        this.onCreate(db);
    }

    public void persist(Long started, Long received, Long transmitted) {
        Log.d(TAG, "persist");
        SQLiteDatabase db = this.getWritableDatabase();

        Long previousReceived = 0L;
        Long previousTransmitted = 0L;

        Cursor cursor = db.rawQuery(QUERY_FIND, new String[] {started.toString()});
        if (cursor.moveToFirst()) {
           previousReceived = Long.parseLong(cursor.getString(1), 10);
           previousTransmitted = Long.parseLong(cursor.getString(2), 10);
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STARTED, started);
        values.put(COLUMN_RECEIVED, received + previousReceived);
        values.put(COLUMN_TRANSMITTED, transmitted + previousTransmitted);

        db.replace(TABLE_NAME, null, values);
        db.close();
    }

    public List<HarvestTrafficEntry> retrieve(Long started) {
        Log.d(TAG, "retrieve");

        List<HarvestTrafficEntry> entries = new ArrayList<HarvestTrafficEntry>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(QUERY_SELECT, new String[] {started.toString()});
        if (cursor.moveToFirst()) {
            do {
                HarvestTrafficEntry entry = new HarvestTrafficEntry();
                entry.started = Long.parseLong(cursor.getString(0), 10);
                entry.received = Long.parseLong(cursor.getString(1), 10);
                entry.transmitted = Long.parseLong(cursor.getString(2), 10);

                Log.d(TAG, String.format("retrieve: %d %d %d", entry.started, entry.received, entry.transmitted));

                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();

        db.close();
        return entries;
    }
}
