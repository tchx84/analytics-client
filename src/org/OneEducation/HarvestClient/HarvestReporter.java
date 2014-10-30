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

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.UnrecoverableKeyException;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.os.Build;
import android.util.Log;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SingleClientConnManager;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestEntry;
import org.OneEducation.HarvestClient.HarvestReporterException;


public class HarvestReporter {

    private Long lastAttempt;
    private Context context;
    private HarvestSettings settings;

    public HarvestReporter(Context _context){
        Log.i("HarvestClient", "creating reporter.");
        context = _context;
        settings = new HarvestSettings(context);
        lastAttempt = settings.getLastReported();
    }

    public Boolean canReport() {
        Log.i("HarvestReporter", "canReport");

        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isAvailable() || !info.isConnected()) {
            Log.i("HarvestReporter", "canReport: not connected");
            return false;
        }

        Long now = System.currentTimeMillis() / 1000L;
        if ((now - lastAttempt) < HarvestSettings.ATTEMPT_INTERVAL) {
            Log.i("HarvestReporter", "canReport: too soon to try");
            return false;
        }
        if ((now - settings.getLastReported()) < HarvestSettings.REPORT_INTERVAL) {
            Log.i("HarvestReporter", "canReport: too son to report");
            return false;
        }

        return true;
    }

    public void report(List<HarvestEntry> entries) throws HarvestReporterException {
       Log.i("HarvestService", "reporting");

       lastAttempt = System.currentTimeMillis() / 1000L;
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
       DefaultHttpClient httpclient = getSecuredHttpClient();
       if (httpclient == null) {
           throw new HarvestReporterException("ops!");
       }

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

    private DefaultHttpClient getSecuredHttpClient() {
        Log.i("HarvestReporter", "getSecuredHttpClient");

        SSLSocketFactory sslFactory = null;
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream stream = context.getResources().getAssets().open("analytics.crt");

            Certificate certificate = factory.generateCertificate(stream);
            stream.close();

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            sslFactory = new SSLSocketFactory(keyStore);
            sslFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        } catch (IOException e) {
            Log.e("HarvestReporter", "certificate", e);
        } catch (CertificateException e) {
            Log.e("HarvestReporter", "certificate", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e("HarvestReporter", "certificate", e);
        } catch (KeyStoreException e) {
            Log.e("HarvestReporter", "certificate", e);
        } catch (KeyManagementException e) {
            Log.e("HarvestReporter", "certificate", e);
        } catch (UnrecoverableKeyException e) {
            Log.e("HarvestReporter", "certificate", e);
        }

        if (sslFactory == null) {
            return null;
        }

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", sslFactory, 443));
        
        DefaultHttpClient tmpClient = new DefaultHttpClient();
        SingleClientConnManager connManager = new SingleClientConnManager(tmpClient.getParams(), registry);
        DefaultHttpClient httpClient = new DefaultHttpClient(connManager, tmpClient.getParams());

        return httpClient;        
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
