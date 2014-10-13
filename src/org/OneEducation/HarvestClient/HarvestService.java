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
import java.lang.Integer;
import java.lang.String;
import java.lang.System;

import android.util.Log;
import android.os.IBinder;
import android.content.Intent;
import android.content.Context;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import android.app.Service;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;

import org.OneEducation.HarvestClient.HarvestJournal;
import org.OneEducation.HarvestClient.HarvestSettings;

public class HarvestService extends Service {

    private Integer MAX_TASKS = 1; 
    private HarvestJournal journal;

    private HarvestReporter reporter;
    private Long lastReported;

    @Override
    public void onCreate() {
        Log.i("HarvestService", "is created.");
        journal = new HarvestJournal(this);

        reporter = new HarvestReporter();
        lastReported = System.currentTimeMillis() / 1000L;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HarvestService", "is running.");
        processActivity();
        reportActivity();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public void onDestroy() {
        Log.i("HarvestService", "is destroyed.");
        journal.dump();
    }

    private void processActivity() {
        ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(MAX_TASKS);
        if (tasks.isEmpty()) {
            Log.i("HarvesyService", "no tasks are running.");
            return;
        }

        RunningTaskInfo task = tasks.get(0);
        ComponentName activity = task.baseActivity;
        String packageName = activity.getPackageName();
        String applicationLabel = "unknown";

        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationLabel = (String)packageManager.getApplicationLabel(applicationInfo);
        }
        catch (NameNotFoundException e) {
            String error = String.format("cannot find package name %s", packageName);
            Log.e("HarvestService", error);
        }

        journal.store(packageName, task.id);
        journal.display(); 
    }

    private void reportActivity() {
        Long now = System.currentTimeMillis() / 1000L;

        if ((now - lastReported) > HarvestSettings.REPORT) {
            List<List<String>> data = journal.getData();
            reporter.report(data);
            lastReported = now;
        }
    }
}
