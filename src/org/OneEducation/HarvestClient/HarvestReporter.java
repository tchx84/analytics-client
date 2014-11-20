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
import java.lang.Long;
import java.lang.String;
import java.lang.Boolean;
import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Build;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestEntry;
import org.OneEducation.HarvestClient.HarvestReporterTask;

public class HarvestReporter {

    private Long lastAttempt;
    private Context context;
    private HarvestSettings settings;
    private HarvestReporterTask previousTask;

    public HarvestReporter(Context _context){
        Log.i("HarvestClient", "creating reporter.");
        context = _context;
        settings = new HarvestSettings(context);
        lastAttempt = settings.getRealNowSeconds();
        previousTask = null;
    }

    public Boolean canReport() {
        Log.i("HarvestReporter", "canReport");

        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isAvailable() || !info.isConnected()) {
            Log.i("HarvestReporter", "canReport: not connected");
            return false;
        }

        if ((settings.getRealNowSeconds() - lastAttempt) < HarvestSettings.ATTEMPT_INTERVAL) {
            Log.i("HarvestReporter", "canReport: too soon to try");
            return false;
        }
        if ((settings.getClockNowSeconds() - settings.getLastReported()) < HarvestSettings.REPORT_INTERVAL) {
            Log.i("HarvestReporter", "canReport: too son to report");
            return false;
        }

        if (previousTask != null && previousTask.getStatus() != Status.FINISHED){
            Log.i("HarvestReporter", "canReport: previous task is still running");
            return false;
        }

        return true;
    }

    public void report(List<HarvestEntry> entries) {
       Log.i("HarvestService", "reporting");
       lastAttempt = settings.getRealNowSeconds();

       JSONArray json = getJSONReport(entries);
       HarvestReporterTask task = new HarvestReporterTask(context);
       task.execute(json.toString());
       previousTask = task;
    }

    private String getUID() {
        String serial = Build.SERIAL;

        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            Log.i("HarvestReporter", "no algorithm");
            return null;
        }

        digest.reset();
        digest.update(serial.getBytes());

        String hash = new BigInteger(1, digest.digest()).toString(16);
        Log.i("HarvestReporter", hash);

        return hash;
    }

    private JSONArray getJSONReport(List<HarvestEntry> entries) {
        JSONObject map = new JSONObject();

        for (HarvestEntry entry : (List<HarvestEntry>) entries) {
            JSONArray sessions = (JSONArray) map.opt(entry.packageName);

            if (sessions == null) {
                sessions = new JSONArray();

                try {
                    map.put(entry.packageName, sessions);
                } catch (JSONException e) {
                    return null;
                }
            } 

            JSONArray session = new JSONArray();
            session.put(entry.started);
            session.put(entry.duration);

            sessions.put(session);
        }

        JSONArray array = new JSONArray();
        array.put(getUID());
        array.put(map);

        return array;
    }
}
