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

import java.lang.System;

import android.R;
import android.util.Log;
import android.os.IBinder;
import android.content.Intent;
import android.content.Context;
import android.app.Service;
import android.app.Notification;

import org.OneEducation.HarvestClient.HarvestWatcher;


public class HarvestService extends Service {

    HarvestWatcher watcher;

    @Override
    public void onCreate() {
        Log.i("HarvestService", "created");
        watcher = new HarvestWatcher(this);
        watcher.run();
        letTheUserKnow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("HarvestService", "started");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public void onDestroy() {
        Log.i("HarvestService", "destroyed");
        watcher.stop();
        stopForeground(true);
    }

    private void letTheUserKnow() {
        Log.d("HarvestService", "letTheUserKnow");
        Notification notification = new Notification.Builder(this)
            .setContentTitle("Analytics")
            .setContentText("The service is running")
            .setSmallIcon(R.drawable.ic_menu_info_details)
            .setAutoCancel(true)
            .build();
        startForeground(1337, notification);
    }
}
