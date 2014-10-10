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
import java.lang.String;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.util.Log;

import org.json.JSONArray;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import org.OneEducation.HarvestClient.HarvestSettings;


public class HarvestReporter {

    public HarvestReporter(){
        Log.i("HarvestClient", "creating reporter.");
    }

    public void report(List<List<String>> data){       
       JSONArray json = new JSONArray(data);

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
       request.setHeader("Content-type", "application/json");

       BasicResponseHandler responseHandler = new BasicResponseHandler();
       DefaultHttpClient httpclient = new DefaultHttpClient();

       try {
           httpclient.execute(request, responseHandler);
       }
       catch (IOException e) {
           Log.e("HarvestClient", "exception", e);
       }
    }
}
