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
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

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

    private HarvestSettings settings;

    public HarvestJournal(Context context) {
        Log.d("HarvestJournal", "created");
        data = new HashMap();
        storage = new HarvestStore(context);
        settings = new HarvestSettings(context);
        lastPersisted = settings.getRealNowSeconds();
        lastStored = settings.getRealNowSeconds();
    }

    private Long getStarted(Long timestamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // if null use today's date, otherwise given timestamp
        if (timestamp != null) {
            calendar.setTimeInMillis(timestamp * 1000L);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis() / 1000L;
    }

    public void store(String packageName) {
        Log.d("HarvestJournal", "store");
        HashMap sessions;

        if (data.containsKey(packageName)){
            sessions = (HashMap) data.get(packageName);
        } else {
            sessions = new HashMap();
            data.put(packageName, sessions);
        }

        Long started = getStarted(null);
        HarvestEntry entry;

        if (sessions.containsKey(started)) {
            entry = (HarvestEntry) sessions.get(started);
        } else {
            entry = new HarvestEntry(packageName);
            entry.started = started;
            sessions.put(started, entry);
        }

        Long delta = settings.getRealNowSeconds() - lastStored;
        if (delta > HarvestSettings.INTERVAL) {
            delta = HarvestSettings.INTERVAL;
        }

        entry.increment(delta);
        Log.d("HarvestJournal", String.format("%s %d %d", entry.packageName, entry.started, entry.duration));

        display();
    }

    public Boolean canDump() {
        Log.d("HarvestJournal", "canDump");
        return ((settings.getRealNowSeconds() - lastPersisted) > HarvestSettings.PERSIST);
    }

    public void display() {
        Log.d("HarvestJournal", "display");
        for (String packageName : (Set<String>) data.keySet()) {
            HashMap sessions = (HashMap) data.get(packageName);

            for (Long started : (Set<Long>) sessions.keySet()) {
                HarvestEntry entry = (HarvestEntry) sessions.get(started);
                Log.d("HarvestJournal", String.format("%s %d %d", entry.packageName, entry.started, entry.duration));
            }
        }
    }

    public void dump() {
        Log.d("HarvestJournal", "dump");
        for (String packageName : (Set<String>) data.keySet()) {
            HashMap sessions = (HashMap) data.get(packageName);

            for (Long started : (Set<Long>) sessions.keySet()) {
                HarvestEntry entry = (HarvestEntry) sessions.get(started);
                storage.persist(entry);
            }
        }

        data.clear();
        lastPersisted = settings.getRealNowSeconds();
    }

    public List<HarvestEntry> getEntries() {
        Log.d("HarvestJournal", "getEntries");
        dump();
        return storage.retrieve(getStarted(settings.getLastReported()));
    }
}
