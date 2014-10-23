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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.String;
import java.lang.Runnable;

import android.os.Handler;
import android.util.Log;
import android.content.Context;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;

import org.OneEducation.HarvestClient.HarvestJournal;
import org.OneEducation.HarvestClient.HarvestEntry;
import org.OneEducation.HarvestClient.HarvestReporterException;


public class HarvestWatcher implements Runnable {

    private Integer MAX_TASKS = 1;
    private List<String> BLACKLIST = new ArrayList<String>(Arrays.asList("android",
                                                                         "com.android.launcher",
                                                                         "com.android.settings",
                                                                         "com.android.systemui"));
    private HarvestJournal journal;
    private HarvestReporter reporter;
    private Handler handler;
    private Context context;

    public HarvestWatcher(Context _context){
        Log.i("HarvestWatcher", "created");

        journal = new HarvestJournal(_context);
        reporter = new HarvestReporter();
        handler =  new Handler();
        context = _context;
    }

    public void run(){
        Log.i("HarvestWatcher", "run");
        processActivity();
        reportActivity();
        handler.postDelayed(this, HarvestSettings.INTERVAL * 1000L);
    }

    public void stop(){
        Log.i("HarvestWatcher", "stop");
        handler.removeCallbacks(this);
        journal.dump();
    }

    private void processActivity(){
        Log.i("HarvestWatcher", "processActivity");
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(MAX_TASKS);
        if (tasks.isEmpty()) {
            Log.i("HarvestWatcher", "no task were found");
            return;
        }

        RunningTaskInfo task = tasks.get(0);
        ComponentName activity = task.baseActivity;
        String packageName = activity.getPackageName();
        String applicationLabel = "unknown";

        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationLabel = (String)packageManager.getApplicationLabel(applicationInfo);
        }
        catch (NameNotFoundException e) {
            String error = String.format("cannot find package name %s", packageName);
            Log.e("HarvestWatcher", error);
        }

        if (!BLACKLIST.contains(packageName)) {
           journal.store(packageName, task.id);
        }

        if (journal.canDump()){
            journal.dump();
        }
    }

    private void reportActivity() {
        Log.i("HarvestWatcher", "reportActivity");
        if (reporter.canReport()) {
            List<HarvestEntry> entries = journal.getEntries();

            try {
                reporter.report(entries);
            } catch (HarvestReporterException e) {
                Log.i("HarvestWatcher", "reportActivity failed");
                return;
            }

            journal.empty();
        }
    }
}
