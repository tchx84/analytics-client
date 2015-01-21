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

import java.lang.Long;
import java.util.List;
import java.util.ArrayList;

import android.util.Log;
import android.content.Context;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStats;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestEntry;


class HarvestJournalNative {

    private Context context;
    private HarvestSettings settings;

    public HarvestJournalNative (Context _context) {
        context = _context;
        settings = new HarvestSettings(context);
    }

    public List<HarvestEntry> getEntries() {
        Log.d("HarvestJournalNative", "getEntries");

        List<HarvestEntry> entries = new ArrayList<HarvestEntry>();

        Long since = settings.getStarted(settings.getLastReported()) * 1000L;
        Long to = System.currentTimeMillis();
        UsageStatsManager manager = (UsageStatsManager)context.getSystemService("usagestats");
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, since, to);

        for (UsageStats stat: stats) {
           HarvestEntry entry = new HarvestEntry(stat.getPackageName());
           entry.started = stat.getFirstTimeStamp() / 1000L;
           entry.duration = stat.getTotalTimeInForeground() / 1000L;
           entries.add(entry);

           String message = String.format("getEntries: %s %s %s", entry.packageName, entry.started, entry.duration);
           Log.d("HarvestJournalNative", message);
        }

        return entries;
    }
}
