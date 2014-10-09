package org.OneEducation.HarvestClient;

import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

public class HarvestService extends Service {

    @Override
    public void onCreate() {
        Log.i("HarvestService", "is created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HarvestService", "is running.");
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
}
