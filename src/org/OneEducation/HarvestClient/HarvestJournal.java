package org.OneEducation.HarvestClient;

import java.lang.Long;
import java.lang.Integer;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Set;

import android.util.Log;


class HarvestJournalEntry {

    public String packageName;
    public Long timestamp;
    public Integer count;

    HarvestJournalEntry (String _packageName) {
        packageName = _packageName;
        timestamp = System.currentTimeMillis() / 1000L;
        count = 0;
    }

    public void increment(Long delta) {
        count += delta;
    }
}


public class HarvestJournal {

    public HashMap data;
    private Long lastStored;

    HarvestJournal() {
        Log.i("HarvestService", "creating journal.");
        data = new HashMap();
        lastStored = System.currentTimeMillis() / 1000L;
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
        entry.increment(delta);

        sessions.put(id, entry);
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
}
