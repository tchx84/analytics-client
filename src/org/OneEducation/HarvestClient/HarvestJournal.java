package org.OneEducation.HarvestClient;

import java.lang.Long;
import java.lang.Integer;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Set;
import java.util.List;

import android.util.Log;
import android.content.Context;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestStore;

class HarvestJournalEntry {

    public String packageName;
    public Long timestamp;
    public Long count;

    HarvestJournalEntry (String _packageName) {
        packageName = _packageName;
        timestamp = System.currentTimeMillis() / 1000L;
        count = 0L;
    }

    public void increment(Long delta) {
        count += delta;
    }
}


public class HarvestJournal {

    private HashMap data;
    private Long lastStored;

    private HarvestStore storage;
    private Long lastPersisted;

    public HarvestJournal(Context context) {
        Log.i("HarvestService", "creating journal.");
        data = new HashMap();
        lastStored = System.currentTimeMillis() / 1000L;

        storage = new HarvestStore(context);
        lastPersisted = lastStored;
    }

    public void store(String packageName, Integer id) {
        HashMap sessions;

        if (data.containsKey(packageName)){
            sessions = (HashMap) data.get(packageName);
        } else {
            sessions = new HashMap();
            data.put(packageName, sessions);
        }

        HarvestJournalEntry entry;

        if (sessions.containsKey(id)) {
            entry = (HarvestJournalEntry) sessions.get(id);
        } else {
            entry = new HarvestJournalEntry(packageName);
        }

        Long now = System.currentTimeMillis() / 1000L;
        Long delta = now - lastStored;
        if (delta > HarvestSettings.INTERVAL) {
            delta = HarvestSettings.INTERVAL;
        }

        entry.increment(delta);
        sessions.put(id, entry);

        if ((now - lastPersisted) > HarvestSettings.PERSIST) {
            dump();
            lastPersisted = now;
        }
    }

    public void dump() {
        Log.i("HarvestClient", "dumping data.");
        for (String packageName : (Set<String>) data.keySet()) {
            HashMap sessions = (HashMap) data.get(packageName);

            for (Integer id : (Set<Integer>) sessions.keySet()) {
                HarvestJournalEntry entry = (HarvestJournalEntry) sessions.get(id);
                storage.persist(entry.timestamp, packageName, entry.count);
            }
        }
    }

    public void display() {
        for (String packageName : (Set<String>) data.keySet()) {
            HashMap sessions = (HashMap) data.get(packageName);

            for (Integer id : (Set<Integer>) sessions.keySet()) {
                HarvestJournalEntry entry = (HarvestJournalEntry) sessions.get(id);
                String message = String.format("%s (%d): %d for %d", packageName, id, entry.timestamp, entry.count);
                Log.i("HarvestService", message);
            }
        }
    }

    public List<List<String>> getData() {
        dump();
        return storage.retrieve();
    }
}
