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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestEntry;

public class HarvestReporter {

    private Long lastReported;

    public HarvestReporter(){
        Log.i("HarvestClient", "creating reporter.");
        lastReported = System.currentTimeMillis() / 1000L;
    }

    public Boolean canReport() {
        Long now = System.currentTimeMillis() / 1000L;
        if ((now - lastReported) > HarvestSettings.REPORT) {
            return true;
        }
        return false;
    }

    public void report(List<HarvestEntry> entries){
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
           return;
       }

       lastReported = System.currentTimeMillis() / 1000L;
       Log.i("HarvestService", "successfully reported");
    }

    private String getUID() {
        return "";
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
