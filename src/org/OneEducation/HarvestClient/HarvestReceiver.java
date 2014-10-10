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

import java.util.Calendar;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;

import org.OneEducation.HarvestClient.HarvestService;
import org.OneEducation.HarvestClient.HarvestSettings;

public class HarvestReceiver extends BroadcastReceiver
{
    private Long INTERVAL = HarvestSettings.INTERVAL * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            prepareService(context);
        }
    }

    private void prepareService(Context context) {
        Intent intent = new Intent(context, HarvestService.class);
        PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();

        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL, pending);
    }
}
