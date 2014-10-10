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
