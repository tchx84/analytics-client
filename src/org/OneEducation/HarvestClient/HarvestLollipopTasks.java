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
import java.lang.String;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import android.util.Log;
import android.content.Context;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStats;

import java.util.Calendar;


class RecentUseComparator implements Comparator<UsageStats> {

    @Override
    public int compare(UsageStats lhs, UsageStats rhs) {
        return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed())?-1:(lhs.getLastTimeUsed() == rhs.getLastTimeUsed())?0:1;
    }
}

class HarvestLollipopTasks {

    private String TAG = "HarvestLollipopTasks";
    private Context context;

    public HarvestLollipopTasks (Context _context) {
        context = _context;
    }

    public String getActiveTask() {
        Log.d(TAG, "getActiveTask");

        // the range queries for today's stats
        long to = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(to);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long since = calendar.getTimeInMillis();

        String rangeMessage = String.format("getActiveTask: since %d, to %d", since, to);
        Log.d(TAG, rangeMessage);

        UsageStatsManager manager = (UsageStatsManager)context.getSystemService("usagestats");
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, since, to);

        if (stats == null || stats.isEmpty()) {
            return null;
        }

        // sort by stats by .getLastTimeUsed()
        RecentUseComparator comparator = new RecentUseComparator();
        Collections.sort(stats, comparator);

        // first must be
        UsageStats stat = stats.get(0);
        String statMessage = String.format("getActiveTask: %s", stat.getPackageName());
        Log.d(TAG, statMessage);

        return stat.getPackageName();
    }
}
