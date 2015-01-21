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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;


class HarvestTrafficStats {

    static String TAG = "HarvestTrafficStats";
    static String PROC_STATS_START = "^(\\s+)(\\w+):.*";
    static String PROC_STATS_SKIP = "^(\\s+)(lo):.*";
    static String PROC_STATS_SEPARATOR = "\\s+";
    static String PROC_STATS_PATH = "/proc/net/dev";

    private Long currentRx;
    private Long currentTx;

    public HarvestTrafficStats() {
        currentRx = -1L;
        currentTx = -1L;
        refreshStats();
    }

    private void refreshStats() {
        Log.d(TAG, "refreshStats");

        try {
            String line;
            BufferedReader ProcNetDev = new BufferedReader(new FileReader(PROC_STATS_PATH));

           currentRx = 0L;
           currentTx = 0L;

           while ((line = ProcNetDev.readLine()) != null) {
               if (line.matches(PROC_STATS_SKIP)) {
                   continue;
               }

               if (line.matches(PROC_STATS_START)) {
                   String[] stats = line.split(PROC_STATS_SEPARATOR);
                   currentRx += Long.parseLong(stats[2], 10);
                   currentTx += Long.parseLong(stats[10], 10);
               }
           }

           ProcNetDev.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
        } catch (IOException e) {
            Log.e(TAG, "Something unexpected happened");
        }
    }

    public Long getTotalRxBytes() {
        return currentRx;
    }

    public Long getTotalTxBytes () {
        return currentTx;
    }
}
