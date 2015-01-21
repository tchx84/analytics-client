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

import java.lang.Boolean;

import android.app.Activity;
import android.app.AppOpsManager;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import org.OneEducation.HarvestClient.HarvestService;


public class HarvestActivity extends Activity
{
    private String TAG = "HarvestActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        launchService();
        launchPermissions();
    }

    private void launchService(){
        Intent serviceIntent = new Intent(this, HarvestService.class);
        startService(serviceIntent);
    }

    private void launchPermissions(){
        if (needPermissions()){
            Intent permissionsIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(permissionsIntent);
        } else {
            Log.d(TAG, "already has permissions");
        }
    }

    private Boolean needPermissions(){
        try {
            PackageManager packageManager = this.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.getPackageName(), 0);

            AppOpsManager appOpsManager = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
            int ops = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                                   applicationInfo.uid,
                                                   applicationInfo.packageName);

            return  (ops != AppOpsManager.MODE_ALLOWED);
        } catch (NameNotFoundException e) {
            return true;
        }
    }
}
