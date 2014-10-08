package org.OneEducation.HarvestClient;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;

import org.OneEducation.HarvestClient.HarvestActivity;

public class HarvestReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent starterIntent = new Intent(context, HarvestActivity.class);
            starterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(starterIntent);
        }
    }
}
