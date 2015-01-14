/*
 * Copyright (C) 2015 Martin Abente Lahaye - martin.abente.lahaye@gmail.com.
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

import java.lang.Long;
import java.util.List;

import android.util.Log;
import android.content.Context;

import org.OneEducation.HarvestClient.HarvestSettings;
import org.OneEducation.HarvestClient.HarvestTrafficStore;
import org.OneEducation.HarvestClient.HarvestTrafficEntry;


class HarvestTrafficJournal {

    private String TAG = "HarvestTrafficJournal";

    private Long lastRx;
    private Long lastTx;
    private Long currentDeltaRx;
    private Long currentDeltaTx;
    private Long lastStored;
    private Long lastDumped;
    private Context context;
    private HarvestSettings settings;
    private HarvestTrafficStore store;

    HarvestTrafficJournal(Context _context, Long initialRx, Long initialTx) {
        Log.d(TAG, "created");

        lastRx = initialRx;
        lastTx = initialTx;
        currentDeltaRx = 0L;
        currentDeltaTx = 0L;
        context = _context;
        settings = new HarvestSettings(context);
        store = new HarvestTrafficStore(context);
        lastStored = settings.getRealNowSeconds();
        lastDumped = settings.getRealNowSeconds();
    }

   public Boolean canStore() {
       Log.d(TAG, "canStore");

       if ((settings.getRealNowSeconds() - lastStored) < settings.TRAFFIC_INTERVAL) {
           Log.d(TAG, "canStore: too soon to get stats");
           return false;
       }

       return true;
   }

   public Boolean canDump() {
       Log.d(TAG, "canDump");

       if ((settings.getRealNowSeconds() - lastDumped) < settings.TRAFFIC_PERSIST) {
           Log.d(TAG, "canDump: too soon to dump stats");
           return false;
       }

       return true;
   }

   public void store(Long currentRx, Long currentTx) {
       Log.d(TAG, "store");

       if (currentRx < lastRx || currentTx < lastTx) {
           Log.d(TAG, "store: Rx or Tx has reset");
           currentDeltaRx += currentRx;
           currentDeltaTx += currentTx;
       } else {
           currentDeltaRx += currentRx - lastRx;
           currentDeltaTx += currentTx - lastTx;
       }

       lastRx = currentRx;
       lastTx = currentTx;
       lastStored = settings.getRealNowSeconds();

       Log.d(TAG, currentDeltaRx.toString());
       Log.d(TAG, currentDeltaTx.toString());
   }

   public void dump() {
       Log.d(TAG, "dump");

       Long started = settings.getStarted(null);
       store.persist(started, currentDeltaRx, currentDeltaTx);

       currentDeltaRx = 0L;
       currentDeltaTx = 0L;
       lastDumped = settings.getRealNowSeconds();
   }

   public List<HarvestTrafficEntry> getEntries() {
       Log.d(TAG, "getEntries");

       // dump everything to disk before report
       dump();

       Long started = settings.getStarted(settings.getLastReported());
       return store.retrieve(started);
   }

}
