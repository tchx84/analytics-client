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

import java.lang.String;
import java.lang.Boolean;
import java.io.InputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.UnrecoverableKeyException;
import java.io.UnsupportedEncodingException;

import android.os.AsyncTask;	
import android.util.Log;
import android.content.Context;

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


class HarvestReporterTask extends AsyncTask<String, Void, Boolean> {

    private Context context;
    private HarvestSettings settings;

    public HarvestReporterTask(Context _context){
        context = _context;
        settings = new HarvestSettings(context);
    }

    @Override
    protected Boolean doInBackground(String... data) {
       Log.d("HarvestReporterTask", "doInBackground");

       StringEntity entity;
       try {
           entity = new StringEntity(data[0]);
       } catch (UnsupportedEncodingException e) {
           Log.e("HarvestReporterTask", "doInBackground", e);
           return false;
       }

       HttpPost request = new HttpPost(HarvestSettings.SERVER);
       request.setEntity(entity);
       request.setHeader("Accept", "application/json");
       request.setHeader("x-api-key", HarvestSettings.KEY);
       request.setHeader("Content-type", "application/json");

       BasicResponseHandler responseHandler = new BasicResponseHandler();
       DefaultHttpClient httpClient = getSecuredHttpClient();

       if (httpClient == null) {
           Log.e("HarvestReporterTask", "doInBackground: no secured client");
           return false;
       }

       try {
           httpClient.execute(request, responseHandler);
       }
       catch (IOException e) {
           Log.e("HarvestReporterTask", "doInBackground", e);
           return false;
       }

       return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result == true) {
            settings.setLastReported(settings.getClockNowSeconds());
            Log.i("HarvestReporterTask", "onPostExecute: reported sucessfully");
        }
    }

    private DefaultHttpClient getSecuredHttpClient() {
        Log.d("HarvestReporterTask", "getSecuredHttpClient");

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
            Log.e("HarvestReporterTask", "getSecuredHttpClient", e);
        } catch (CertificateException e) {
            Log.e("HarvestReporterTask", "getSecuredHttpClient", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e("HarvestReporterTask", "getSecuredHttpClient", e);
        } catch (KeyStoreException e) {
            Log.e("HarvestReporterTask", "getSecuredHttpClient", e);
        } catch (KeyManagementException e) {
            Log.e("HarvestReporterTask", "getSecuredHttpClient", e);
        } catch (UnrecoverableKeyException e) {
            Log.e("HarvestReporterTask", "getSecuredHttpClient", e);
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
}
