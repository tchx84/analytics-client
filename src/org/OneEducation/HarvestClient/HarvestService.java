package org.OneEducation.HarvestClient;

import android.os.IBinder;
import android.app.Service;
import android.content.Intent;

public class HarvestService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
}
