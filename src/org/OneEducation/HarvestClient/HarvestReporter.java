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

import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.lang.Long;
import java.lang.String;
import java.lang.Boolean;
import java.lang.System;
import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.os.Build;
import android.util.Log;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestEntry;
import org.OneEducation.HarvestClient.HarvestReporterException;


public class HarvestReporter {

    private HarvestSettings settings;

    public HarvestReporter(Context context){
        Log.i("HarvestClient", "creating reporter.");
        settings = new HarvestSettings(context);
    }

    public Boolean canReport() {
        Long now = System.currentTimeMillis() / 1000L;
        if ((now - settings.getLastReported()) > HarvestSettings.REPORT_INTERVAL) {
            return true;
        }
        return false;
    }

    public void report(List<HarvestEntry> entries) throws HarvestReporterException {
       Log.i("HarvestService", "reporting");

       JSONArray json = getJSONReport(entries);

       StringEntity string;
       try {
           string = new StringEntity(json.toString());
       }
       catch (UnsupportedEncodingException e) {
           Log.e("HarvesyClient", "cannot serialize data.");
           return;
       }

       HttpPost request = new HttpPost(HarvestSettings.SERVER);
       request.setEntity(string);
       request.setHeader("Accept", "application/json");
       request.setHeader("x-api-key", HarvestSettings.KEY);
       request.setHeader("Content-type", "application/json");

       BasicResponseHandler responseHandler = new BasicResponseHandler();
       DefaultHttpClient httpclient = new DefaultHttpClient();

       try {
           httpclient.execute(request, responseHandler);
       }
       catch (IOException e) {
           Log.e("HarvestClient", "exception", e);
           throw new HarvestReporterException("ops!");
       }

       settings.setLastReported(System.currentTimeMillis() / 1000L);
       Log.i("HarvestService", "successfully reported");
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
