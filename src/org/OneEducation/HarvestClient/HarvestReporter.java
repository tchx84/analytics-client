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
