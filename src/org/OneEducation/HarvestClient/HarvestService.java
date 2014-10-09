package org.OneEducation.HarvestClient;

import java.util.List;
import java.lang.Integer;
import java.lang.String;

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


public class HarvestService extends Service {

    private Integer MAX_TASKS = 1; 

    @Override
    public void onCreate() {
        Log.i("HarvestService", "is created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HarvestService", "is running.");
        processActivity();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public void onDestroy() {
        Log.i("HarvestService", "is destroyed.");
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

        String message = String.format("%d : %s (%s)", task.id, applicationLabel, packageName);
        Log.i("HarvestService", message);
    }
}
