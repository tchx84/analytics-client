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

import java.lang.Long;
import java.lang.Integer;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import android.util.Log;
import android.content.Context;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestStore;
import org.OneEducation.HarvestClient.HarvestEntry;

public class HarvestJournal {

    private HashMap data;
    private Long lastStored;

    private HarvestStore storage;
    private Long lastPersisted;

    public HarvestJournal(Context context) {
        Log.i("HarvestJournal", "created");
        data = new HashMap();
        lastStored = System.currentTimeMillis() / 1000L;

        storage = new HarvestStore(context);
        lastPersisted = lastStored;
    }

    public void store(String packageName, Integer id) {
        Log.i("HarvestJournal", "store");
        HashMap sessions;

        if (data.containsKey(packageName)){
            sessions = (HashMap) data.get(packageName);
        } else {
            sessions = new HashMap();
            data.put(packageName, sessions);
        }

        HarvestEntry entry;

        if (sessions.containsKey(id)) {
            entry = (HarvestEntry) sessions.get(id);
        } else {
            entry = new HarvestEntry(packageName);
        }

        Long now = System.currentTimeMillis() / 1000L;
        Long delta = now - lastStored;
        if (delta > HarvestSettings.INTERVAL) {
            delta = HarvestSettings.INTERVAL;
        }

        entry.increment(delta);
        sessions.put(id, entry);
        Log.i("HarvestJournal", String.format("%s %d %d %d", entry.packageName, id, entry.started, entry.duration));

        display();
    }

    public Boolean canDump() {
        Log.i("HarvestJournal", "canDump");
        Long now = System.currentTimeMillis() / 1000L;
        return ((now - lastPersisted) > HarvestSettings.PERSIST);
    }

    public void display() {
        Log.i("HarvestJournal", "display");
        for (String packageName : (Set<String>) data.keySet()) {
            HashMap sessions = (HashMap) data.get(packageName);

            for (Integer id : (Set<Integer>) sessions.keySet()) {
                HarvestEntry entry = (HarvestEntry) sessions.get(id);
                Log.i("HarvestJournal", String.format("%s %d %d %d", entry.packageName, id, entry.started, entry.duration));
            }
        }
    }

    public void dump() {
        Log.i("HarvestJournal", "dump");
        for (String packageName : (Set<String>) data.keySet()) {
            HashMap sessions = (HashMap) data.get(packageName);

            for (Integer id : (Set<Integer>) sessions.keySet()) {
                HarvestEntry entry = (HarvestEntry) sessions.get(id);
                storage.persist(entry);
            }
        }

        lastPersisted = System.currentTimeMillis() / 1000L;
    }

    public List<HarvestEntry> getEntries() {
        Log.i("HarvestJournal", "getEntries");
        dump();
        return storage.retrieve();
    }
}
