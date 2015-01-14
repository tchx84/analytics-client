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

import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.String;
import java.lang.Runnable;

import android.os.Handler;
import android.util.Log;
import android.content.Context;
import android.content.ComponentName;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;

import org.OneEducation.HarvestClient.HarvestJournal;
import org.OneEducation.HarvestClient.HarvestReporter;
import org.OneEducation.HarvestClient.HarvestEntry;
import org.OneEducation.HarvestClient.HarvestTrafficStats;
import org.OneEducation.HarvestClient.HarvestTrafficJournal;
import org.OneEducation.HarvestClient.HarvestTrafficEntry;

public class HarvestWatcher implements Runnable {

    private Integer MAX_TASKS = 1;
    private List<String> BLACKLIST = new ArrayList<String>(Arrays.asList("android",
                                                                         "com.android.launcher",
                                                                         "com.android.settings",
                                                                         "com.android.systemui"));
    private HarvestJournal journal;
    private HarvestTrafficJournal trafficJournal;
    private HarvestReporter reporter;
    private Handler handler;
    private Context context;

    public HarvestWatcher(Context _context){
        Log.d("HarvestWatcher", "created");

        journal = new HarvestJournal(_context);
        reporter = new HarvestReporter(_context);
        handler =  new Handler();
        context = _context;

        // discard initial values because we can not make any
        // assumptions regarding whether or not these values
        // were accounted before. Only measure traffic since
        // the service started.
        HarvestTrafficStats stats = new HarvestTrafficStats();
        Long initialRx = stats.getTotalRxBytes();
        Long initialTx = stats.getTotalTxBytes();
        trafficJournal = new HarvestTrafficJournal(_context, initialRx, initialTx);
    }

    public void run(){
        Log.d("HarvestWatcher", "run");

        processActivity();
        persistActivity();

        processTraffic();
        persistTraffic();

        reportActivity();

        handler.postDelayed(this, HarvestSettings.INTERVAL * 1000L);
    }

    public void stop(){
        Log.d("HarvestWatcher", "stop");
        handler.removeCallbacks(this);
        journal.dump();
        trafficJournal.dump();
    }

    private void persistActivity() {
        if (journal.canDump()){
            journal.dump();
        }
    }

    private void processActivity(){
        Log.d("HarvestWatcher", "processActivity");
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(MAX_TASKS);
        if (tasks.isEmpty()) {
            Log.d("HarvestWatcher", "no task were found");
            return;
        }

        RunningTaskInfo task = tasks.get(0);
        ComponentName activity = task.baseActivity;
        String packageName = activity.flattenToString();
        String applicationLabel = "unknown";

        for (String blacklisted: BLACKLIST) {
            if (packageName.startsWith(blacklisted)){
                return;
            }
        }

        journal.store(packageName);
    }

    private void persistTraffic() {
        if (trafficJournal.canDump()) {
            trafficJournal.dump();
        }
    }

    private void processTraffic() {
        Log.d("HarvestWatcher", "processTraffic");

        if (!trafficJournal.canStore()) {
            return;
        }

        HarvestTrafficStats stats = new HarvestTrafficStats();
        Long rx = stats.getTotalRxBytes();
        Long tx = stats.getTotalTxBytes();

        Log.d("HarvestWatcher", rx.toString());
        Log.d("HarvestWatcher", tx.toString());

        if (rx == -1L || tx == -1L) {
            Log.e("HarvestWatcher", "processTraffic: cannot get stats");
            return;
        }

        trafficJournal.store(rx, tx);
    }

    private void reportActivity() {
        Log.d("HarvestWatcher", "reportActivity");
        if (reporter.canReport()) {
            List<HarvestTrafficEntry> trafficEntries = trafficJournal.getEntries();
            List<HarvestEntry> entries = journal.getEntries();
            reporter.report(entries, trafficEntries);
        }
    }
}
